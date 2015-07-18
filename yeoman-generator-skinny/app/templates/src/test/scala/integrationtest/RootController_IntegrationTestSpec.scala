package integrationtest

import _root_.controller.Controllers
import skinny.test.{SkinnyFlatSpec, SkinnyTestSupport}

class RootController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport {
  Controllers.root.mount(servletContextHandler.getServletContext)

  it should "show top page" in {
    get("/") {
      status should equal(200)
    }
  }

}
