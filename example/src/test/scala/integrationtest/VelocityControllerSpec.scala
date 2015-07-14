package integrationtest

import skinny.test.SkinnyTestSupport
import controller.Controllers
import skinny.test.scalatest.SkinnyFlatSpec

class VelocityControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.velocity, "/*")

  it should "render as expected" in {
    get("/velocity/") {
      status should equal(200)
    }
  }

}
