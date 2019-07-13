package lenses.exchange.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveUnwrappedDecoder, deriveUnwrappedEncoder}

case class MoneyAmount(value: Double) extends AnyVal
object MoneyAmount {
  implicit val encoder: Encoder[MoneyAmount] = deriveUnwrappedEncoder[MoneyAmount]
  implicit val decoder: Decoder[MoneyAmount] = deriveUnwrappedDecoder[MoneyAmount]
}
