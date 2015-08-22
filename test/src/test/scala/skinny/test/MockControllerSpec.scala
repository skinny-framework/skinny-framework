package skinny.test

import org.scalatest._
import skinny._

class MockControllerSpec extends FunSpec with Matchers {
  System.setProperty(SkinnyEnv.PropertyKey, "test")

  class AppTest extends SkinnyController {
    def useUrl = url("/foo")
    def useUrl2 = url(app.hoge, "a" -> "b")
  }
  object app extends AppTest with Routes {
    get("/api/useUrl")(useUrl)
    get("/api/useUrl2")(useUrl2)

    val hoge = get("hoge")("aaaa").as('hoge)
  }

  class ApiTest extends SkinnyApiController with Routes {
    def useUrl = url("/foo")
    def useUrl2 = url(api.hoge, "a" -> "b")
  }
  object api extends ApiTest with Routes {
    get("/api/useUrl")(useUrl)
    get("/api/useUrl2")(useUrl2)

    val hoge = get("hoge")("aaaa").as('hoge)
  }

  describe("MockController") {
    it("works with #url") {
      val controller = new AppTest with MockController

      val result = controller.useUrl
      result should equal("/foo")

      val result2 = controller.useUrl2
      result2 should equal("/hoge?a=b")
    }
  }

}
