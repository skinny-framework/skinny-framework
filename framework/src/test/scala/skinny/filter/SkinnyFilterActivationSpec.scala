package skinny.filter

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.SkinnyApiController
import skinny.routing.Routes

class SkinnyFilterActivationSpec extends ScalatraFlatSpec with skinny.Logging {

  object Controller extends SkinnyApiController with SkinnyFilterActivation with Routes {
    override def detectTooManyErrorFilterRegistrationAsAnErrorAtSkinnyMicroBase = true
    def index                                                                   = "ok"
    get("/")(index).as("index")
  }
  addFilter(Controller, "/*")

  it should "not waste memory" in {
    (1 to 500).foreach { _ =>
      get("/") { status should equal(200) }
    }
    // if possible leak detected, this status will be 500
    get("/") { status should equal(200) }
  }

}
