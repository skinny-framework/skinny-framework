package skinny.controller.feature

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController
import skinny.micro.async.AsyncOperations

import scala.concurrent.duration._

class FutureOpsFeatureSpec extends ScalatraFlatSpec {
  behavior of "FutureOpsFeature"

  object Controller extends SkinnyController with AsyncOperations with Routes {
    def index = {
      val f = futureWithRequest { req =>
        req.getContextPath
      }
      awaitFutures(1.seconds)(f)
    }
    def withContext = {
      val f = FutureWithContext { implicit ctx =>
        contextPath
      }
      awaitFutures(1.seconds)(f)
    }
    get("/")(index).as("index")
    get("/context")(withContext).as("context")
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
