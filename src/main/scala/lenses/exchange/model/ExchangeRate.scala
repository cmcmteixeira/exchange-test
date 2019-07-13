package lenses.exchange.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}

case class ExchangeRate(value: Double) extends AnyVal

object ExchangeRate {
  implicit val encoder: Encoder[ExchangeRate] = deriveUnwrappedEncoder[ExchangeRate]
  implicit val decoder: Decoder[ExchangeRate] = deriveUnwrappedDecoder[ExchangeRate]
}
