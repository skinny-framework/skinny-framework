package integrationtest

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport
import controller.Controllers

class ThymeleafControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.thymeleaf, "/*")

  it should "render as expected" in {
    get("/thymeleaf/") {
      status should equal(200)
      body should include("three")
      body should include("nested-<span>1</span>")
      body should include("one-&gt;1")
      body should include("Hello, Thymeleaf!")
    }
  }

}
