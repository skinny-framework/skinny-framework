package skinny.controller.feature

import org.scalatra.test.scalatest._
import skinny.Routes
import skinny.controller.SkinnyApiController

class CORSFeatureSpec extends ScalatraFlatSpec {

  behavior of "CORSFeature"

  class SampleController extends SkinnyApiController with CORSFeature {
    beforeAction() {
      contentType = "application/json"
    }

    def showAlice = toJSONString(Map("name" -> "Alice", "age" -> 23))
  }
  val controller = new SampleController with Routes {
    val creationUrl = get("/alice")(showAlice).as("alice")
  }

  addFilter(controller, "/*")

  it should "have CORS headers" in {
    get("/alice") {
      status should equal(200)
      header("Access-Control-Allow-Origin") should equal("*")
    }
  }

}
