package skinny.controller.implicits

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.ParamType
import skinny.controller.SkinnyController

class ParamsPermitImplicitsSpec extends ScalatraFlatSpec {
  behavior of "ParamsPermitImplicits"

  object Controller extends SkinnyController {
    def index = {
      val permittedParams = params.permit("name" -> ParamType.String)
      permittedParams.params should equal(Map("name" -> ("Alice" -> ParamType.String)))
      "ok"
    }
    get("/")(index)
  }
  addFilter(Controller, "/*")

  it should "be available" in {
    get("/", Map("name" -> "Alice", "age" -> "20")) {
      status should equal(200)
    }
  }
}
