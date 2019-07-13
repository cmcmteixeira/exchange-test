package lenses.exchange

import cats.effect._
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import kamon.influxdb.InfluxDBReporter
import kamon.system.SystemMetrics
import lenses.exchange.app.{App, Config}
import app._
import cats.implicits._
import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  def config: SyncIO[Config] =
    SyncIO(
      ConfigFactory.load()
    ).map(c => buildConfig(c).unsafeRunSync)

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] =
    Resource
      .pure[SyncIO, Unit](())
      .evalMap(_ => config)
      .flatMap(c => mainThreadPool(c.mainThreadPool))
      .map(ExecutionContext.fromExecutor(_))

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val ec: ExecutionContext = executionContext
    (for {
      config <- Resource.liftF(config.to[IO])
      _      <- Resource.liftF(IO(Kamon.loadReportersFromConfig()))
      _      <- Resource.make(IO(Kamon.addReporter(new InfluxDBReporter())))(r => IO(r.cancel()).void)
      _      <- Resource.make(IO(SystemMetrics.startCollecting()))(_ => IO.unit)
      client <- httpClient(config.httpClient)(executionContext, contextShift)
    } yield App(executionContext, contextShift, timer, client, config)).use(app => server(app).use(_ => IO.never))
  }

}
