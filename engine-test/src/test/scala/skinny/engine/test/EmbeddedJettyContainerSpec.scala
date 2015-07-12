package skinny.engine.test

import javax.servlet.http._

import org.scalatest._

class EmbeddedJettyContainerSpec extends WordSpec
    with Matchers
    with BeforeAndAfter
    with EmbeddedJettyContainer
    with HttpComponentsClient {

  before { start() }
  after { stop() }

  private val servlet = new HttpServlet {
    override def doGet(req: HttpServletRequest, res: HttpServletResponse) = {
      val hasDefault = getServletContext.getNamedDispatcher("default") != null
      res.addHeader("X-Has-Default-Servlet", hasDefault.toString)
      res.getWriter.print("Hello, world")
    }
  }
  addServlet(servlet, "/*")

  "An embedded jetty container" should {
    "respond to a hello world servlet" in {
      get("/") { body should equal("Hello, world") }
    }

    "have a default servlet" in {
      get("/") { header("X-Has-Default-Servlet") should equal("true") }
    }
  }

}
