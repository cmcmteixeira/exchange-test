package lenses.exchange

import com.typesafe.config.{Config => RawConfig}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.namemappers.implicits.hyphenCase
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import kamon.http4s.middleware.client.{KamonSupport => KClient}
import kamon.http4s.middleware.server.{KamonSupport => KServer}
import java.util.concurrent.{ExecutorService, Executors}

import cats.effect.{ContextShift, IO, Resource, SyncIO, Timer}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import kamon.executors.util.ContextAwareExecutorService
import kamon.executors.{Executors => KExecutors}
import cats.implicits._
import lenses.exchange.http.ExchangeRateClient.ExchangeRateClientConfig
import lenses.exchange.middleware.LoggingMiddleware
import net.ceedubs.ficus.readers.{AnyValReaders, StringReader, ValueReader}
import org.http4s.Uri
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.server.Server

import scala.concurrent.ExecutionContext

package object app {

  def mainThreadPool(config: ThreadPoolConfig): Resource[SyncIO, ExecutorService] =
    Resource
      .make(
        for {
          rawExecutor     <- SyncIO(Executors.newFixedThreadPool(config.size))
          instrumentedExc <- SyncIO(ContextAwareExecutorService(rawExecutor))
          registration    <- SyncIO(KExecutors.register("main", rawExecutor))
        } yield (rawExecutor, instrumentedExc, registration)
      ) {
        case (executor, _, registration) => SyncIO(executor.shutdown()) *> SyncIO(registration.cancel()).void
      }
      .map(_._2)

  def httpClient(config: HttpClientConfig)(implicit ec: ExecutionContext, cs: ContextShift[IO]): Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](ec)
      .withExecutionContext(ec)
      .withMaxWaitQueueLimit(config.maxWaitQueue)
      .withIdleTimeout(config.idleTimeout)
      .withResponseHeaderTimeout(config.responseHeaderTimeout)
      .resource
      .map(KClient(_))

  def buildConfig(config: RawConfig): IO[Config] =
    IO {
      implicit val uriReader: ValueReader[Uri] = StringReader.stringValueReader.map(Uri.unsafeFromString)
      val mainThreadPool                       = config.as[ThreadPoolConfig]("main-thread-pool")
      val httpServer                           = config.as[HttpServerConfig]("http-server")
      val httpClient                           = config.as[HttpClientConfig]("http-client")
      val exchangeRateClient                   = config.as[ExchangeRateClientConfig]("exchange-service")
      Config(mainThreadPool, httpServer, httpClient, exchangeRateClient)
    }

  def blazeServerBuilder(config: HttpServerConfig)(implicit timer: Timer[IO], cs: ContextShift[IO], ec: ExecutionContext): BlazeServerBuilder[IO] =
    BlazeServerBuilder[IO]
      .withExecutionContext(implicitly)
      .bindHttp(config.port, config.address)

  def server(app: App): Resource[IO, Server[IO]] =
    BlazeServerBuilder(IO.ioConcurrentEffect(app.cs), app.timer)
      .withHttpApp(KServer(LoggingMiddleware(app.routes)).orNotFound)
      .withExecutionContext(app.ec)
      .bindHttp(app.config.httpServer.port, app.config.httpServer.address)
      .resource
}
