package controller

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import skinny.test.MockController

class RootControllerSpec extends AnyFunSpec with Matchers {

  describe("RootController") {
    it("shows top page") {
      val controller = new RootController with MockController
      controller.index
      controller.contentType should equal("text/html; charset=utf-8")
      controller.renderCall.map(_.path) should equal(Some("/root/index"))
    }
  }

}
