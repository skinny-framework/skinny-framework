package integrationtest

import controller.Controllers
import skinny.test.{ SkinnyFlatSpec, SkinnyTestSupport }

class FreeMarkerControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.freemarker, "/*")

  // TODO: Fix this test
//  it should "render as expected" in {
//    get("/freemarker/") {
//      status should equal(200)
//    }
//  }

}
