package skinny.controller

import skinny.test.scalatest.SkinnyFlatSpec

class SessionInjectorControllerSpec extends SkinnyFlatSpec {

  addFilter(SessionInjectorController, "/*")

  it should "renew session attributes" in {
    session {
      put("/session", "hoge" -> SessionInjectorController.serialize("aaa")) {
        status should equal(200)
      }
      get("/session.json") {
        body should include(""""hoge":"aaa"""")
      }
    }
  }

}

