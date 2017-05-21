package skinny.controller.feature

import org.scalatra.test.scalatest._
import skinny.controller.SkinnyController

class ExplicitRedirectFeatureSpec extends ScalatraFlatSpec {

  behavior of "ExplicitRedirectFeature"

  object App extends SkinnyController {
    def r    = redirect("/foo")
    def r301 = redirect301("/foo")
    def r302 = redirect302("/foo")
    def r303 = redirect303("/foo")

    get("/r")(r)
    get("/r301")(r301)
    get("/r302")(r302)
    get("/r303")(r303)
  }

  addFilter(App, "/*")

  it should "have redirect" in {
    get("/r") {
      status should equal(302)
    }
  }
  it should "have redirect301" in {
    get("/r301") {
      status should equal(301)
    }
  }
  it should "have redirect302" in {
    get("/r302") {
      status should equal(302)
    }
  }
  it should "have redirect303" in {
    get("/r303") {
      status should equal(303)
    }
  }

}
