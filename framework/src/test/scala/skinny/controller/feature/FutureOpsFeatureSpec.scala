package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController
import skinny.engine.async.AsyncOperations

import scala.concurrent.duration._

class FutureOpsFeatureSpec extends ScalatraFlatSpec {
  behavior of "FutureOpsFeature"

  object Controller extends SkinnyController with AsyncOperations with Routes {
    def index = {
      awaitFutures(1.seconds)(futureWithRequest(req => "ok"))
    }
    def withContext = {
      awaitFutures(1.seconds)(futureWithContext(ctx => "ok"))
    }
    get("/")(index).as('index)
    get("/context")(withContext).as('context)
  }
  addFilter(Controller, "/*")

  it should "have #futureWithRequest" in {
    get("/") {
      status should equal(200)
    }
  }
  it should "have #futureWithContext" in {
    get("/context") {
      status should equal(200)
    }
  }

}
