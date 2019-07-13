package lenses.exchange.health

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._
import cats.implicits._

case class Unhealthy(reason: String) extends AnyVal
object Unhealthy {
  implicit val encoder: Encoder[Unhealthy]                 = deriveUnwrappedEncoder[Unhealthy]
  implicit val decoder: Decoder[Unhealthy]                 = deriveUnwrappedDecoder[Unhealthy]
  implicit val entityDecoder: EntityDecoder[IO, Unhealthy] = jsonOf[IO, Unhealthy]
  implicit val entityEncoder: EntityEncoder[IO, Unhealthy] = jsonEncoderOf[IO, Unhealthy]
}

trait HealthCheckService {
  def health(): IO[Either[Unhealthy, Unit]]
}

object HealthCheckService extends StrictLogging {

  def apply(): HealthCheckService =
    () =>
      for {
        _ <- IO(logger.info("Running Healthcheck"))
      } yield ().asRight
}
