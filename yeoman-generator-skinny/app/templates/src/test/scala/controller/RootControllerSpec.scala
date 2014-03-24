package controller

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import skinny.test.MockController

class RootControllerSpec extends FunSpec with ShouldMatchers {

  describe("RootController") {
    it("shows top page") {
      val controller = new RootController with MockController
      controller.index
      controller.contentType should equal("text/html; charset=utf-8")
      controller.renderCall.map(_.path) should equal(Some("/root/index"))
    }
  }

}
