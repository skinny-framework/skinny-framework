package skinny.controller

import org.scalatra.test.scalatest._

class SessionInjectorControllerSpec extends ScalatraFlatSpec {

  addFilter(SessionInjectorController, "/*")

  it should "renew session attributes" in {
    session {
      put("/session",
        "hoge" -> SessionInjectorController.serialize("aaa")) {}
      get("/session.json") {
        body should include(""""hoge":"aaa"""")
      }
    }
  }

}

