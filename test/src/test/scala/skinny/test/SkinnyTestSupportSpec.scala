package skinny.test

import skinny._
import skinny.controller.SkinnyController
import skinny.filter.SkinnySessionFilter

case class Foo(str: String, value: Int) extends java.io.Serializable

class SkinnyTestSupportSpec extends SkinnyFlatSpec with SkinnyTestSupport with Serializable {

  object SessionInjectorTest extends SkinnyController with Routes with DBSettings with SkinnySessionFilter {

    def show = session("inject")
    get("/session-injector-result")(show)

    def showSkinnySession = skinnySession.getAttribute("inject").orNull
    get("/skinny-session-injector-result")(showSkinnySession)

    def useUrl = url("/foo")
    get("/useUrl")(useUrl)

    val hoge    = get("hoge")("aaaa").as("hoge")
    def useUrl2 = url(hoge)
    get("/useUrl2")(useUrl2)
  }
  addFilter(SessionInjectorTest, "/*")

  class ApiTest extends SkinnyApiController with Routes {
    def useUrl = url("/foo")
    get("/api/useUrl")(useUrl)

    val hoge    = get("hoge")("aaaa").as("hoge")
    def useUrl2 = url(hoge)
    get("/api/useUrl2")(useUrl2)
  }
  addFilter(new ApiTest, "/*")

  it should "serialize string" in {
    val obj = "foo"
    val s   = SessionInjector.serialize(obj)
    val act = SessionInjector.deserialize(s)
    act should equal(obj)
  }

  it should "serialize object" in {
    val obj = Foo("foo", 100)
    val s   = SessionInjector.serialize(obj)
    val act = SessionInjector.deserialize(s)
    act should equal(obj)
  }

  // TODO: unstable tests on Travis
  /*
  it should "inject session attributes for testing" in {
    withSession("inject" -> "foo") {
      get("/session-injector-result") {
        body should equal("foo")
      }
    }
  }

  it should "inject session objects for testing" in {
    withSession("inject" -> Foo("foo", 100)) {
      get("/session-injector-result") {
        body should equal("Foo(foo,100)")
      }
    }
  }

  it should "inject skinny session attributes for testing" in {
    withSkinnySession("inject" -> "foo") {
      get("/skinny-session-injector-result") {
        body should equal("foo")
      }
    }
  }

  it should "inject skinny session objects for testing" in {
    withSkinnySession("inject" -> Foo("foo", 100)) {
      get("/skinny-session-injector-result") {
        body should equal("Foo(foo,100)")
      }
    }
  }
   */

  it should "work with #url" in {
    get("/useUrl") {
      status should equal(200)
    }
    get("/useUrl2") {
      status should equal(200)
    }
  }

  it should "work with #url when using SkinnyApiController" in {
    get("/api/useUrl") {
      status should equal(200)
    }
    get("/api/useUrl2") {
      status should equal(200)
    }
  }

}
