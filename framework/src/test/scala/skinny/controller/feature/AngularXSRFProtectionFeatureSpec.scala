package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyApiController

class AngularXSRFProtectionFeatureSpec extends ScalatraFlatSpec {

  behavior of "AngularXSRFCookieProviderFeature"

  class Controller extends SkinnyApiController with AngularXSRFProtectionFeature {
    protectFromForgery()
    def token  = "welcome"
    def create = "ok"
  }
  val controller = new Controller with Routes {
    get("/token")(token).as("token")
    post("/")(create).as("create")
  }

  addFilter(controller, "/*")

  it should "reject CSRF request" in {
    post("/") {
      status should equal(403)
      header("Set-Cookie") should include regex ("XSRF-TOKEN=\\w+")
    }
  }

  it should "accept valid request" in {
    session {
      get("/token") {
        status should equal(200)
        val token = header("Set-Cookie").split("=")(1).split(";")(0).trim()
        post("/", "", Map(AngularJSSpecification.xsrfHeaderName -> token)) {
          status should equal(200)
        }
      }
    }
  }

}
