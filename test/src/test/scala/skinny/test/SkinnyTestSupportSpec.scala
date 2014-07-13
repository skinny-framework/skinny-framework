package skinny.test

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._
import skinny.controller.SkinnyController
import skinny.filter.SkinnySessionFilter

case class Foo(str: String, value: Int) extends java.io.Serializable

class SkinnyTestSupportSpec extends ScalatraFlatSpec with SkinnyTestSupport
    with Serializable {

  object SessionInjectorTest extends SkinnyController with Routes
      with DBSettings
      with SkinnySessionFilter {

    def show = session("inject")
    get("/session-injector-result")(show)

    def showSkinnySession = skinnySession.getAttribute("inject").orNull
    get("/skinny-session-injector-result")(showSkinnySession)
  }
  addFilter(SessionInjectorTest, "/*")

  it should "serialize string" in {
    val obj = "foo"
    val s = SessionInjector.serialize(obj)
    val act = SessionInjector.deserialize(s)
    act should equal(obj)
  }

  it should "serialize object" in {
    val obj = Foo("foo", 100)
    val s = SessionInjector.serialize(obj)
    val act = SessionInjector.deserialize(s)
    act should equal(obj)
  }

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
}

