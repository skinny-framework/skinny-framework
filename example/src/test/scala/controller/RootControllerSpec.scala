package controller

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport

class RootControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.root, "/*")

  it should "show top page" in {
    get("/") {
      status should equal(200)
    }
  }

  it should "renew session attributes" in {
    session {
      get("/session/renew", "locale" -> "ja", "returnTo" -> "/") {
        status should equal(302)
      }
      get("/") {
        status should equal(200)
        body should include("プログラマ")
      }
    }
  }

}
