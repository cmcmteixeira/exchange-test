package lenses.exchange.routes

import cats.effect.IO
import lenses.exchange.health.HealthCheckService
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class AppStatusRoutes(heathCheckService: HealthCheckService) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      heathCheckService.health().flatMap {
        case Left(unhealthy) => InternalServerError(unhealthy)
        case Right(_)        => Ok("")
      }
  }
}
