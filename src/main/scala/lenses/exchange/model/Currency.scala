package lenses.exchange.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}

sealed trait Currency extends Any
case class FromCurrency(value: String) extends AnyVal with Currency
case class ToCurrency(value: String)   extends AnyVal with Currency

object FromCurrency {
  implicit val encoder: Encoder[FromCurrency] = deriveUnwrappedEncoder[FromCurrency]
  implicit val decoder: Decoder[FromCurrency] = deriveUnwrappedDecoder[FromCurrency]
}

object ToCurrency {
  implicit val encoder: Encoder[ToCurrency] = deriveUnwrappedEncoder[ToCurrency]
  implicit val decoder: Decoder[ToCurrency] = deriveUnwrappedDecoder[ToCurrency]
}
