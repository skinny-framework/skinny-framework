package example

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine.AsyncSkinnyEngineServlet

import scala.concurrent.Future

class StableRequestSpec extends ScalatraFlatSpec {

  addServlet(new AsyncSkinnyEngineServlet {
    get("/foo") { implicit ctx =>
      request.setAttribute("foo", "bar")
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getAttribute("foo")
      }
    }
    get("/getAuthType") { implicit ctx =>
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getAuthType
      }
    }
    get("/getHeader") { implicit ctx =>
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getHeader("X-REQUEST-ID")
      }
    }
    get("/getPathInfo") { implicit ctx =>
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getPathInfo
      }
    }
    get("/getMethod") { implicit ctx =>
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getMethod
      }
    }
    get("/getMethod") { implicit ctx =>
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getMethod
      }
    }
    get("/getSession/ok") { implicit ctx =>
      request.getSession
    }
    get("/getSession/ng") { implicit ctx =>
      Future {
        Thread.sleep(100) // To get the container to give up the request
        request.getSession
      }
    }
  }, "/app/*")

  it should "handle request attributes" in {
    (1 to 20).foreach { _ =>
      get("/app/foo") {
        status should equal(200)
        body should equal("bar")
      }
    }
  }

  it should "handle getAuthType" in {
    (1 to 20).foreach { _ =>
      get("/app/getAuthType") {
        status should equal(200)
        body should equal("")
      }
    }
  }

  it should "handle getHeader" in {
    (1 to 20).foreach { _ =>
      get(uri = "/app/getHeader", headers = Map("X-REQUEST-ID" -> "123")) {
        status should equal(200)
        body should equal("123")
      }
    }
  }
  it should "handle getPathInfo" in {
    (1 to 20).foreach { _ =>
      get("/app/getPathInfo") {
        status should equal(200)
        body should equal("/getPathInfo")
      }
    }
  }
  it should "handle getMethod" in {
    (1 to 20).foreach { _ =>
      get("/app/getMethod") {
        status should equal(200)
        body should equal("GET")
      }
    }
  }

  it should "allow getSession" in {
    (1 to 20).foreach { _ =>
      get("/app/getSession/ok") {
        status should equal(200)
      }
    }
  }
  it should "prohibit getSession" in {
    (1 to 20).foreach { _ =>
      get("/app/getSession/ng") {
        status should equal(500)
      }
    }
  }
}
