package integrationtest

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport
import controller.Controllers

class FreeMarkerControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.freemarker, "/*")

  it should "render as expected" in {
    get("/freemarker/") {
      status should equal(200)
    }
  }

}
