package controller

import org.scalatest._
import skinny.test.MockController

class ScaldiControllerSpec extends FunSpec with Matchers {

  describe("ScaldiController") {

    def createMockController = new ScaldiController with MockController

    describe("uses injected Service") {
      it("works") {
        val controller = createMockController
        controller.prepareParams("value" -> "foo")
        controller.index should equal("foo")
        controller.status should equal(200)
      }
    }

  }
}
