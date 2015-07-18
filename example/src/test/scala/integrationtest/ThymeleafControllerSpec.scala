package integrationtest

import controller.Controllers
import skinny.test.{ SkinnyFlatSpec, SkinnyTestSupport }

class ThymeleafControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport {

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
