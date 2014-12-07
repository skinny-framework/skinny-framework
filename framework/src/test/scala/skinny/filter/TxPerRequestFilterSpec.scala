package skinny.filter

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.SkinnyApiController
import skinny.routing.Routes

class TxPerRequestFilterSpec extends ScalatraFlatSpec {

  object Controller extends SkinnyApiController with TxPerRequestFilter with Routes {
    def index = "ok"
    get("/")(index).as('index)
  }
  addFilter(Controller, "/*")

  it should "be available" in {
    get("/") {
      status should equal(200)
    }
  }

}
