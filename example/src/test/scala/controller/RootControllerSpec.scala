package controller

import service._
import org.scalatra.test.scalatest._
import skinny._
import skinny.test.SkinnyTestSupport

class RootControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  class EchoServiceMock extends EchoService {
    override def echo(s: String): String = s.toUpperCase
  }

  addFilter(Controllers.root, "/*")
  addFilter(new RootController with Routes {
    override val echoService = new EchoServiceMock
    get("/mock/?")(index).as('index)
  }, "/*")

  it should "show top page" in {
    get("/?echo=abcdEFG") {
      status should equal(200)
      body should include("abcdEFG")
    }
    get("/mock/?echo=abcdEFG") {
      status should equal(200)
      body should include("ABCDEFG")
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

  it should "show error" in {
    get("/error") {
      status should equal(500)
    }
  }

}
