package integrationtest

import skinny.test.{SkinnyFlatSpec, SkinnyTestSupport}
import controller.Controllers

class VelocityControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.velocity, "/*")

  it should "render as expected" in {
    get("/velocity/") {
      status should equal(200)
    }
  }

}
