package skinny.test

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._

class SkinnyTestSupportSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  object SessionInjectorTest extends SkinnyController with Routes {
    def show = session("inject")
    get("/session-injector-result")(show)
  }
  addFilter(SessionInjectorTest, "/*")

  it should "inject session attributes for testing" in {
    withSession("inject" -> "foo") {
      get("/session-injector-result") {
        body should equal("foo")
      }
    }
  }

}

