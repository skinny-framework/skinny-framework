package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyApiController

class ChunkedResponseFeatureSpec extends ScalatraFlatSpec {

  behavior of "ChunkedResponseFeature"

  class Controller extends SkinnyApiController with ChunkedResponseFeature {
    def index = {
      writeChunk("abc".getBytes)
    }
  }
  val controller = new Controller with Routes {
    get("/")(index).as("index")
  }

  addFilter(controller, "/*")

  it should "return chunked response" in {
    get("/") {
      status should equal(200)
      body should equal("abc")
    }
  }

}
