package controller

import org.scalatest._
import skinny.test.MockController
import skinny.DBSettings

class RootControllerSpec extends FunSpec with Matchers with DBSettings {

  describe("RootController") {

    def createMockController = new RootController with MockController

    describe("skinny session filter") {
      it("works") {
        val controller = createMockController
        controller.index
        controller.status should equal(200)
      }
    }

  }
}
