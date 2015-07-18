package integrationtest

import _root_.controller._
import skinny.test.SkinnyFlatSpec

class SampleTxApiControllerSpec extends SkinnyFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.sampleTxApi, "/*")

  it should "show 500 error" in {
    get("/api/error") {
      status should equal(500)
    }
  }

}
