package lenses.exchange.routes

import cats.effect.IO
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import lenses.exchange.model.{ExchangeRate, FromCurrency, InvalidSymbols, MoneyAmount, ToCurrency}
import lenses.exchange.services.ExchangeService
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.io._

class ExchangeRoutes(exchangeService: ExchangeService) {
  import ExchangeRoutes._

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "convert" =>
      for {
        conversionReq <- req.as[RateConversionRequest]
        conversion    <- exchangeService.exchange(conversionReq.fromCurrency, conversionReq.toCurrency)(conversionReq.amount)
        response <- conversion match {
          case Right((rate, amount)) => Ok(RateConversionResponse(rate, amount, conversionReq.amount))
          case Left(InvalidSymbols)  => BadRequest(s"One of the currencies provided does not seem to be supported.")
        }
      } yield response
  }
}

object ExchangeRoutes {
  def apply(exchangeService: ExchangeService): ExchangeRoutes = new ExchangeRoutes(exchangeService)

  case class RateConversionRequest(fromCurrency: FromCurrency, toCurrency: ToCurrency, amount: MoneyAmount)
  object RateConversionRequest {
    implicit val encoder: Encoder[RateConversionRequest]                 = deriveEncoder[RateConversionRequest]
    implicit val decoder: Decoder[RateConversionRequest]                 = deriveDecoder[RateConversionRequest]
    implicit val entityDecoder: EntityDecoder[IO, RateConversionRequest] = jsonOf[IO, RateConversionRequest]
    implicit val entityEncoder: EntityEncoder[IO, RateConversionRequest] = jsonEncoderOf[IO, RateConversionRequest]
  }
  case class RateConversionResponse(exchange: ExchangeRate, amount: MoneyAmount, original: MoneyAmount)
  object RateConversionResponse {
    implicit val encoder: Encoder[RateConversionResponse]                 = deriveEncoder[RateConversionResponse]
    implicit val decoder: Decoder[RateConversionResponse]                 = deriveDecoder[RateConversionResponse]
    implicit val entityDecoder: EntityDecoder[IO, RateConversionResponse] = jsonOf[IO, RateConversionResponse]
    implicit val entityEncoder: EntityEncoder[IO, RateConversionResponse] = jsonEncoderOf[IO, RateConversionResponse]
  }
}
