package skinny.controller.feature

import skinny.controller._

import skinny.Routes
import skinny.test.SkinnyFlatSpec

class VelocityTemplateEngineFeatureSpec extends SkinnyFlatSpec {

  behavior of "VelocityTemplateEngineFeature"

  val resourcesDir = "velocity/src/test/resources"
  servletContextHandler.setResourceBase(resourcesDir)

  object VelocityController extends SkinnyController with VelocityTemplateEngineFeature {
    def a = render("foo/a")
    def b = render("foo/b")
    def d = render("foo/d")

    get("/velocity/a")(a)
    get("/velocity/b")(b)
    get("/velocity/d")(d)
  }
  addFilter(VelocityController, "/*")

  it should "render an Velocity template" in {
    get("/velocity/a") {
      status should be(200)
      body should include("<p>This is Velocity template A</p>")
    }

    get("/velocity/b") {
      status should be(200)
      body should include("<p>This is Velocity template B</p>")
    }

    get("/velocity/d") {
      status should be(200)
      body should include("[1, 2, 3, 4, 5]")
    }
  }
}
