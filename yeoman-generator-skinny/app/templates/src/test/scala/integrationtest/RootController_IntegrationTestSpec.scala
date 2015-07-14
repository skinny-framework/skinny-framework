package integrationtest

import _root_.controller.Controllers
import skinny.test.SkinnyTestSupport
import skinny.test.scalatest.SkinnyFlatSpec

class RootController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport {
  Controllers.root.mount(servletContextHandler.getServletContext)

  it should "show top page" in {
    get("/") {
      status should equal(200)
    }
  }

}
