package integrationtest

import _root_.controller._
import org.scalatra.test.scalatest._

class SampleTxApiControllerSpec extends ScalatraFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.sampleTxApi, "/*")

  it should "show 500 error" in {
    get("/api/error") {
      status should equal(500)
    }
  }

}
