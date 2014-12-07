package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class FutureOpsFeatureSpec extends ScalatraFlatSpec {
  behavior of "FutureOpsFeature"

  object Controller extends SkinnyController with FutureOpsFeature with Routes {
    def index = {
      awaitFutures(1.seconds)(futureWithRequest(req => "ok"))
    }
    get("/")(index).as('index)
  }
  addFilter(Controller, "/*")

  it should "have methods" in {
    get("/") {
      status should equal(200)
    }
  }

}
