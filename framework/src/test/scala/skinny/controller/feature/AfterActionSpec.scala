package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController

class AfterActionSpec extends ScalatraFlatSpec {
  behavior of "afterAction"

  object After1 extends SkinnyController with Routes {
    get("/1") { response.writer.write("0") }.as("index")
    afterAction() { response.writer.write("1") }
    afterAction() { response.writer.write("2") }
  }
  object After2 extends SkinnyController with Routes {
    get("/2") { response.writer.write("OK") }.as("index")
    afterAction() { response.writer.write(" Computer") }
  }
  addFilter(After1, "/*")
  addFilter(After2, "/*")

  "afterAction" should "be controller-local" in {
    get("/1") {
      body should equal("012")
    }
    get("/2") {
      body should equal("OK Computer")
    }
  }

}
