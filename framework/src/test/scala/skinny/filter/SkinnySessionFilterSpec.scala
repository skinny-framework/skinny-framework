package skinny.filter

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.controller.SkinnyApiController
import skinny.routing.Routes

class SkinnySessionFilterSpec extends ScalatraFlatSpec {
  behavior of "SkinnySessionFilter"

  object Controller extends SkinnyApiController with Routes {
    get("/")("ok").as('index)
  }
  addFilter(Controller, "/*")

  it should "work" in {
    get("/") {
      status should equal(200)
    }
  }

}
