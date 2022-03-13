package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.Routes
import skinny.controller.SkinnyApiController

class AngularXSRFCookieProviderFeatureSpec extends ScalatraFlatSpec {

  behavior of "AngularXSRFCookieProviderFeature"

  class Controller extends SkinnyApiController with AngularXSRFCookieProviderFeature {
    def showAlice = toJSONString(Map("name" -> "Alice", "age" -> 23))
  }
  val controller = new Controller with Routes {
    get("/alice")(showAlice).as("alice")
  }

  addFilter(controller, "/*")

  it should "return Set-Cookie header" in {
    get("/alice") {
      status should equal(200)
      header("Set-Cookie") should include regex ("XSRF-TOKEN=\\w+")
    }
  }
}
