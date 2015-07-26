package example

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine.AsyncSkinnyEngineServlet

import scala.concurrent.Future

class StableResponseSpec extends ScalatraFlatSpec {

  addServlet(new AsyncSkinnyEngineServlet {
    get("/foo") { implicit ctx =>
      request.setAttribute("foo", "bar")
      Future {
        Thread.sleep(100) // To get the container to give up the request
        response.getOutputStream.write(request.getAttribute("foo").toString.getBytes)
      }
    }
  }, "/app/*")

  it should "write request attributes" in {
    (1 to 20).foreach { _ =>
      get("/app/foo") {
        status should equal(200)
        body should equal("bar")
      }
    }
  }

}
