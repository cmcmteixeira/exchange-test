package lenses.exchange.services

import cats.effect.IO
import cats.implicits._
import lenses.exchange.http.ExchangeRateClient
import lenses.exchange.model.{ExchangeRate, FromCurrency, InvalidSymbols, MoneyAmount, ToCurrency}
import lenses.unit.DefaultSpec
import org.mockito.Mockito._

class ExchangeServiceSpec extends DefaultSpec {
  class Feature {
    val fromCurrency: FromCurrency  = FromCurrency("EUR")
    val amount: MoneyAmount         = MoneyAmount(10.00)
    val toCurrency: ToCurrency      = ToCurrency("GBP")
    val exchangeRate: ExchangeRate  = ExchangeRate(0.898)
    val conversionResult            = MoneyAmount(8.98)
    val clientS: ExchangeRateClient = mock[ExchangeRateClient]
    val service: ExchangeService    = ExchangeService(clientS)
  }
  "exchange()" should "convert a money amount based on the response from the exchange client service" in withIO {
    val f = new Feature
    import f._
    when(clientS.fetchExchangeRate(fromCurrency, toCurrency)).thenReturn(exchangeRate.asRight.pure[IO])
    for {
      result <- service.exchange(fromCurrency, toCurrency)(amount)
    } yield {
      result should ===(Right(exchangeRate -> conversionResult))
    }
  }

  it should "return an InvalidSymbols if the clientS cannot find " in withIO {
    val f = new Feature
    import f._
    when(clientS.fetchExchangeRate(fromCurrency, toCurrency)).thenReturn(InvalidSymbols.asLeft.pure[IO])
    for {
      result <- service.exchange(fromCurrency, toCurrency)(amount)
    } yield {
      result should ===(Left(InvalidSymbols))
    }
  }
}
