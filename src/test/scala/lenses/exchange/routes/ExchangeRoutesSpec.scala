package lenses.exchange.routes

import cats.effect.IO
import cats.implicits._
import lenses.exchange.model.{ExchangeRate, FromCurrency, InvalidSymbols, MoneyAmount, ToCurrency}
import lenses.exchange.routes.ExchangeRoutes.{RateConversionRequest, RateConversionResponse}
import lenses.exchange.services.ExchangeService
import lenses.unit.DefaultSpec
import org.http4s.{Request, Uri}
import org.http4s.client.Client
import org.mockito.Mockito._
import org.http4s.implicits._
import org.http4s.Method._
import org.http4s.Status._

class ExchangeRoutesSpec extends DefaultSpec {
  class Fixture {

    val fromCurrency: FromCurrency   = FromCurrency("EUR")
    val amount: MoneyAmount          = MoneyAmount(10.00)
    val convertedAmount: MoneyAmount = MoneyAmount(10.00)
    val toCurrency: ToCurrency       = ToCurrency("GBP")
    val exchangeRate: ExchangeRate   = ExchangeRate(0.0898)
    val expectedResult               = RateConversionResponse(exchangeRate, amount, convertedAmount)
    val requestEntity                = RateConversionRequest(fromCurrency, toCurrency, amount)
    val request                      = Request(method = POST, uri = Uri.unsafeFromString("/convert")).withEntity(requestEntity)

    val exchangeS: ExchangeService     = mock[ExchangeService]
    val exchangeRoutes: ExchangeRoutes = new ExchangeRoutes(exchangeS)
    val client: Client[IO]             = Client.fromHttpApp(exchangeRoutes.routes.orNotFound)
  }

  "a call to /convert" should "return the conversion" in withIO {
    val f = new Fixture
    import f._
    when(exchangeS.exchange(fromCurrency, toCurrency)(amount)).thenReturn((exchangeRate, amount).asRight.pure[IO])
    for {
      response <- f.client.expect[RateConversionResponse](request)
    } yield {
      response should ===(expectedResult)
    }
  }

  it should " return BadRequest if a currency is not supported" in withIO {
    val f = new Fixture
    import f._
    when(exchangeS.exchange(fromCurrency, toCurrency)(amount)).thenReturn(InvalidSymbols.asLeft.pure[IO])
    for {
      response <- f.client.fetch(request)(_.status.pure[IO])
    } yield {
      response should ===(BadRequest)
    }
  }

}
