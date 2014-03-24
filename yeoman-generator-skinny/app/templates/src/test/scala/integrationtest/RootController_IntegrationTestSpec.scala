package integrationtest

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport
import _root_.controller.Controllers

class RootController_IntegrationTestSpec extends ScalatraFlatSpec with SkinnyTestSupport {
  Controllers.root.mount(servletContextHandler.getServletContext)

  it should "show top page" in {
    get("/") {
      status should equal(200)
    }
  }

}
