package lenses.exchange.http

import cats.effect.IO
import com.typesafe.scalalogging.StrictLogging
import lenses.exchange.model.{ExchangeRate, FromCurrency, InvalidSymbols, ManagedErrors, ToCurrency}
import org.http4s.{Uri, _}
import org.http4s.client.Client
import org.http4s.dsl.io._
import cats.implicits._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

trait ExchangeRateClient {
  def fetchExchangeRate(from: FromCurrency, to: ToCurrency): IO[Either[ManagedErrors, ExchangeRate]]
}

object ExchangeRateClient {
  case class ExchangeRateClientConfig(basePath: Uri)
  def apply(client: Client[IO], config: ExchangeRateClientConfig): ExchangeRateClient = new ExchangeRateClient with StrictLogging {
    def fetchExchangeRate(from: FromCurrency, to: ToCurrency): IO[Either[ManagedErrors, ExchangeRate]] =
      for {
        _        <- IO(logger.info(s"Fetching exchange from $from to $to."))
        url      <- (config.basePath / "latest").withQueryParam("symbols", to.value).withQueryParam("base", from.value).pure[IO]
        request  <- Request[IO](method = GET, uri = url).pure[IO]
        response <- client.fetch(request)(handleResponse(to))
        _        <- IO(response.fold(e => logger.warn(s"Could not determine exchange rate due to $e"), er => logger.info(s"Exchange rate found: $er")))
      } yield response
  }

  def handleResponse(to: ToCurrency)(response: Response[IO]): IO[Either[ManagedErrors, ExchangeRate]] =
    response.status match {
      case Ok =>
        response.as[ExchangeRateClientResponse].map(_.rates.get(to.value.toUpperCase)).flatMap {
          case None    => IO.raiseError(new RuntimeException("Could not find from rate in currency API response."))
          case Some(e) => ExchangeRate(e).asRight.pure[IO]
        }
      case BadRequest =>
        response.as[ExchangeRateApiError].flatMap {
          case ExchangeRateApiError(message) if message.contains("is not supported") || message.contains("are invalid") =>
            InvalidSymbols.asLeft.pure[IO]
          case unknonwnError => IO.raiseError(new RuntimeException(s"Unknown error from exchange api: $unknonwnError."))
        }
      case _ => IO.raiseError(new RuntimeException(s"Unexpected return code ${response.status} from exchange api."))
    }
  def toSymbolQuery(from: FromCurrency, to: ToCurrency): String =
    s"${from.value},${to.value}"

  case class ExchangeRateApiError(error: String)
  object ExchangeRateApiError {
    implicit val encoder: Encoder[ExchangeRateApiError]                 = deriveEncoder[ExchangeRateApiError]
    implicit val decoder: Decoder[ExchangeRateApiError]                 = deriveDecoder[ExchangeRateApiError]
    implicit val entityDecoder: EntityDecoder[IO, ExchangeRateApiError] = jsonOf[IO, ExchangeRateApiError]
    implicit val entityEncoder: EntityEncoder[IO, ExchangeRateApiError] = jsonEncoderOf[IO, ExchangeRateApiError]
  }

  case class ExchangeRateClientResponse(rates: Map[String, Double])
  object ExchangeRateClientResponse {
    implicit val encoder: Encoder[ExchangeRateClientResponse]                 = deriveEncoder[ExchangeRateClientResponse]
    implicit val decoder: Decoder[ExchangeRateClientResponse]                 = deriveDecoder[ExchangeRateClientResponse]
    implicit val entityDecoder: EntityDecoder[IO, ExchangeRateClientResponse] = jsonOf[IO, ExchangeRateClientResponse]
    implicit val entityEncoder: EntityEncoder[IO, ExchangeRateClientResponse] = jsonEncoderOf[IO, ExchangeRateClientResponse]
  }
}
