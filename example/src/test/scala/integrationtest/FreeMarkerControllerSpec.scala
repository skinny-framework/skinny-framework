package integrationtest

import controller.Controllers
import skinny.test.SkinnyTestSupport
import skinny.test.scalatest.SkinnyFlatSpec

class FreeMarkerControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.freemarker, "/*")

  it should "render as expected" in {
    get("/freemarker/") {
      status should equal(200)
    }
  }

}
