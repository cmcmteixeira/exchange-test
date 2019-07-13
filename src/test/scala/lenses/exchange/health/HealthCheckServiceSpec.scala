package lenses.exchange.health

import lenses.unit.DefaultSpec

class HealthCheckServiceSpec extends DefaultSpec {
  "Healthcheck service" should "return 'right if the service is healthy" in withIO {
    for {
      result <- HealthCheckService().health()
    } yield result shouldBe 'right
  }
}
