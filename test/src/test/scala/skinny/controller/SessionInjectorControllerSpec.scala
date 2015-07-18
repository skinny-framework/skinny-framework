package skinny.controller

import skinny.test.{ SkinnyFlatSpec, SkinnyTestSupport }

class SessionInjectorControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport {

  addFilter(SessionInjectorController, "/*")

  it should "renew session attributes" in {
    session {
      put("/session", "hoge" -> SessionInjectorController.serialize("aaa")) {
        logBodyUnless(200)
        status should equal(200)
      }
      get("/session.json") {
        body should include(""""hoge":"aaa"""")
      }
    }
  }

}

