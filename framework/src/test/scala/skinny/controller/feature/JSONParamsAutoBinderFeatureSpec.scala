package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController
import skinny.json.JSONStringOps

class JSONParamsAutoBinderFeatureSpec extends ScalatraFlatSpec {

  behavior of "JSONParamsAutoBinderFeature"

  object Controller extends SkinnyController with JSONParamsAutoBinderFeature with Routes {
    def index = {
      params.getAs[String]("name") should equal(Some("Alice"))
    }
    post("/")(index).as("index")
  }
  addFilter(Controller, "/*")

  it should "accepts json body as params" in {
    val body    = JSONStringOps.toJSONString(Map("name" -> "Alice"))
    val headers = Map("Content-Type" -> "application/json")
    post("/", body, headers) {
      status should equal(200)
    }
  }

}
