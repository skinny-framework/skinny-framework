package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController

class BeforeActionSpec extends ScalatraFlatSpec {
  behavior of "beforeAction"

  object Before1 extends SkinnyController with Routes {
    get("/1") { response.writer.write("2") }.as("index")
    beforeAction() { response.writer.write("0") }
    beforeAction() { response.writer.write("1") }
  }
  object Before2 extends SkinnyController with Routes {
    get("/2") { response.writer.write("Computer") }.as("index")
    beforeAction() { response.writer.write("OK ") }
  }
  addFilter(Before1, "/*")
  addFilter(Before2, "/*")

  "beforeAction" should "be controller-local" in {
    get("/1") {
      body should equal("012")
    }
    get("/2") {
      body should equal("OK Computer")
    }
  }

}
