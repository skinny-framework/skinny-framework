package controller

import org.scalatest._
import skinny.test.MockController

class DashboardControllerSpec extends FunSpec with Matchers {

  def createMockController = new DashboardController with MockController

  describe("DashboardController") {
    it("works with Futures") {
      val controller = createMockController
      controller.index
      controller.status should equal(200)
    }
  }

}
