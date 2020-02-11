package integrationtest

import _root_.controller._
import service._
import skinny.{ Routes, _ }
import skinny.test.SkinnyFlatSpec

class RootControllerSpec extends SkinnyFlatSpec with unit.SkinnyTesting {

  class EchoServiceMock extends EchoService {
    override def echo(s: String): String = s.toUpperCase
  }

  addFilter(Controllers.root, "/*")
  addFilter(ErrorController, "/*")
  addFilter(new RootController with Routes {
    override val echoService = new EchoServiceMock
    get("/mock/?".r)(index).as("index")
  }, "/*")

  it should "show top page" in {
    get("/?echo=abcdEFG") {
      status should equal(200)
      body should include("abcdEFG")

      header("X-Content-Type-Options") should equal("nosniff")
      header("X-XSS-Protection") should equal("1; mode=block")
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
      body.size should be > 0
    }
    get("/error/runtime") {
      status should equal(500)
      body.size should be > 0
    }
  }

  it should "show nested i18n messages" in {
    get("/nested-i18n", "foo" -> "will be OK") {
      status should equal(200)
    }
    get("/nested-i18n", "foo" -> "will be NG") {
      status should equal(400)
      body should include("foo must include 'OK'")
    }
    session {
      get("/session/renew", "locale" -> "ja", "returnTo" -> "/") {
        status should equal(302)
      }
      get("/nested-i18n", "foo" -> "will be NG") {
        status should equal(400)
        body should include("ふー は 'OK' を含まなければならない")
      }
    }
  }
}
