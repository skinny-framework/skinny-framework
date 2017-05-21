package skinny.bootstrap

import javax.servlet.ServletContext

import org.scalatest._
import skinny.controller.{ SkinnyApiController, SkinnyController, SkinnyServlet }
import skinny.micro.WebApp
import skinny.micro.routing.RouteRegistry

class NOOPServletContextSpec extends FunSpec with Matchers {
  RouteRegistry.init()

  val app1 = new SkinnyController {
    get("/1/echo") { params.get("name") }
  }
  object app2 extends SkinnyServlet {
    post("/2/echo") { params.get("name") }
  }
  // loaded routing DSLs will be displayed
  val app3 = new WebApp {
    get("/3/micro") {}
  }
  object app4 extends SkinnyApiController {
    // unloaded object's configuration won't be displayed
    options("/4/unused") {}
  }

  class Bootstrap extends SkinnyLifeCycle {
    override def initSkinnyApp(ctx: ServletContext): Unit = {
      app1.mount(ctx)
      app2.mount(ctx)
    }
  }

  describe("NOOPServletContext") {
    it("works") {
      (new Bootstrap).initSkinnyApp(NOOPServletContext)
      RouteRegistry.toString() should equal(
        """GET	/1/echo
          |POST	/2/echo
          |GET	/3/micro
          |""".stripMargin
      )
    }
  }

}
