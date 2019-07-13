package lenses.exchange.app

import lenses.exchange.http.ExchangeRateClient.ExchangeRateClientConfig
import scala.concurrent.duration.FiniteDuration

case class ThreadPoolConfig(
    size: Int
)
case class DatabaseConfig(
    driverClassName: String,
    url: String,
    user: String,
    pass: String
)

case class HttpServerConfig(
    port: Int,
    address: String
)

case class HttpClientConfig(
    maxWaitQueue: Int,
    idleTimeout: FiniteDuration,
    responseHeaderTimeout: FiniteDuration
)

case class Config(
    mainThreadPool: ThreadPoolConfig,
    httpServer: HttpServerConfig,
    httpClient: HttpClientConfig,
    exchangeRateService: ExchangeRateClientConfig
)