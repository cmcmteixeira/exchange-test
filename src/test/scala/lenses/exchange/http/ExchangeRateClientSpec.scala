package lenses.exchange.http

import cats.effect.IO
import lenses.exchange.model.{ExchangeRate, FromCurrency, InvalidSymbols, ToCurrency}
import lenses.unit.DefaultSpec
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{HttpRoutes, Uri}
import io.circe.Json
import lenses.exchange.http.ExchangeRateClient.ExchangeRateClientConfig
import org.http4s.circe._

class ExchangeRateClientSpec extends DefaultSpec {
  class Fixture {
    object SymbolsParamMatcher extends QueryParamDecoderMatcher[String]("symbols")
    object BaseParamMatcher    extends QueryParamDecoderMatcher[String]("base")

    val fromCurrency: FromCurrency = FromCurrency("EUR")
    val toCurrency: ToCurrency     = ToCurrency("GBP")
    val exchangeRate: ExchangeRate = ExchangeRate(0.0898)

    val httpClient: Client[IO] = Client.fromHttpApp(
      HttpRoutes
        .of[IO] {
          case GET -> Root / "latest" :? SymbolsParamMatcher(s) +& BaseParamMatcher(b) => {
            if (s == toCurrency.value && b == fromCurrency.value) {
              Ok(Json.obj("rates" -> Json.obj(toCurrency.value -> Json.fromDouble(exchangeRate.value).get)): Json)
            } else if (s != toCurrency.value) {
              BadRequest(Json.obj("error" -> Json.fromString(s"Symbols '$s; is not supported.")): Json)
            } else {
              BadRequest(Json.obj("error" -> Json.fromString(s"Base '$s' is not supported.")): Json)
            }
          }

        }
        .orNotFound)
    val service = ExchangeRateClient(httpClient, ExchangeRateClientConfig(Uri.unsafeFromString("/")))
  }

  "fetchExchangeRate()" should "return an exchange rate for a currency pair" in withIO {
    val f = new Fixture
    import f._
    for {
      result <- service.fetchExchangeRate(fromCurrency, toCurrency)
    } yield {
      result should ===(Right(exchangeRate))
    }
  }

  it should "return an InvalidSymbols currency if the from currency is not supported" in withIO {
    val f = new Fixture
    import f._
    for {
      result <- service.fetchExchangeRate(FromCurrency("NOTSUPPORTED"), toCurrency)
    } yield {
      result should ===(Left(InvalidSymbols))
    }
  }

  it should "return an InvalidSymbols currency if the to currency is not supported" in withIO {
    val f = new Fixture
    import f._
    for {
      result <- service.fetchExchangeRate(fromCurrency, ToCurrency("NOTSUPPORTED"))
    } yield {
      result should ===(Left(InvalidSymbols))
    }
  }

}
