package lenses.exchange.services

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import lenses.exchange.http.ExchangeRateClient
import lenses.exchange.model.{ExchangeRate, FromCurrency, ManagedErrors, MoneyAmount, ToCurrency}

trait ExchangeService {
  def exchange(from: FromCurrency, to: ToCurrency)(amount: MoneyAmount): IO[Either[ManagedErrors, (ExchangeRate, MoneyAmount)]]
}

object ExchangeService {
  def apply(exchangeClient: ExchangeRateClient): ExchangeService = new ExchangeService with StrictLogging {
    override def exchange(from: FromCurrency, to: ToCurrency)(amount: MoneyAmount): IO[Either[ManagedErrors, (ExchangeRate, MoneyAmount)]] =
      for {
        _        <- IO(logger.info(s"Exchanging $amount from $from into $to"))
        exchange <- exchangeClient.fetchExchangeRate(from, to)
        result   <- exchange.map(e => convert(e, amount)).pure[IO]
        _        <- IO(result.fold(e => logger.warn(s"Failed conversion with error $e"), r => logger.info(s"Successfully calculated new amount $r")))
      } yield (exchange, result).mapN((exchange, result) => (exchange, result))

    private def convert(exchangeRate: ExchangeRate, amount: MoneyAmount): MoneyAmount =
      MoneyAmount(amount.value * exchangeRate.value)
  }
}
