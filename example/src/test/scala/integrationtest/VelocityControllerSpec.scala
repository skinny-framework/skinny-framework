package integrationtest

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport
import controller.Controllers

class VelocityControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.velocity, "/*")

  it should "render as expected" in {
    get("/velocity/") {
      status should equal(200)
    }
  }

}
