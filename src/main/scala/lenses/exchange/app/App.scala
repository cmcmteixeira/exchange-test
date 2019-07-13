package lenses.exchange.app

import cats.effect.{ContextShift, IO, Timer}
import lenses.exchange.health.HealthCheckService
import lenses.exchange.http.ExchangeRateClient
import org.http4s.client.Client
import org.http4s.HttpRoutes

import scala.concurrent.ExecutionContext
import lenses.exchange.routes.{AppStatusRoutes, ExchangeRoutes}
import lenses.exchange.services.ExchangeService
import org.http4s.server.Router

trait App {
  def ec: ExecutionContext
  def cs: ContextShift[IO]
  def timer: Timer[IO]
  def client: Client[IO]
  def config: Config
  def routes: HttpRoutes[IO]
}

object App {
  def apply(
      _ec: ExecutionContext,
      _cs: ContextShift[IO],
      _timer: Timer[IO],
      _client: Client[IO],
      _config: Config
  ): App = {
    val healthService   = HealthCheckService()
    val exchangeClient  = ExchangeRateClient(_client, _config.exchangeRateService)
    val exchangeService = ExchangeService(exchangeClient)
    val appRoutes = Router(
      "/_meta" -> new AppStatusRoutes(healthService).routes,
      "/api"   -> new ExchangeRoutes(exchangeService).routes,
    )
    new App {
      override def ec: ExecutionContext   = _ec
      override def cs: ContextShift[IO]   = _cs
      override def timer: Timer[IO]       = _timer
      override def client: Client[IO]     = _client
      override def config: Config         = _config
      override def routes: HttpRoutes[IO] = appRoutes
    }
  }

}
