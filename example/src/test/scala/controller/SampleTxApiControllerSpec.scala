package controller

import org.scalatest._
import skinny.test.MockApiController

class SampleTxApiControllerSpec extends FunSpec with Matchers {

  def createMockController = new SampleTxApiController with MockApiController

  describe("SampleTxApiController") {
    it("throws error") {
      val controller = createMockController
      intercept[RuntimeException] {
        controller.index
      }
    }
  }

}
