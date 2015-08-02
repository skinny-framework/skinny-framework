package example

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine.json.EngineJSONStringOps
import skinny.engine.{ AsyncSkinnyEngineServlet, ServletConcurrencyException }

import scala.concurrent.Future
import scala.concurrent.duration._

class FutureSpec extends ScalatraFlatSpec {

  addServlet(new AsyncSkinnyEngineServlet with EngineJSONStringOps {

    get("/") { implicit ctx =>
      responseAsJSON(params)
    }

    get("/future") { implicit ctx =>
      Future {
        responseAsJSON(params)
      }
    }

    get("/no-future-error") { implicit ctx =>
      responseAsJSON(awaitFutures(3.seconds) {
        Future {
          try {
            responseAsJSON(params)
          } catch {
            case e: ServletConcurrencyException =>
              Map("message" -> e.getMessage)
          }
        }
      })
    }
  }, "/*")

  it should "simply work" in {
    get("/?foo=bar") {
      status should equal(200)
      body should equal("""{"foo":"bar"}""")
    }
  }
  it should "fail with simple Future" in {
    get("/no-future-error?foo=bar") {
      status should equal(200)
      var found = false
      var count = 0
      while (!found && count < 10) {
        if (body.contains("Concurrency Issue Detected")) {
          found = true
        } else {
          count += 1
        }
      }
      found should be(false)
    }
  }
  it should "work with futureWithContext" in {
    get("/future?foo=bar") {
      status should equal(200)
      body should equal("""{"foo":"bar"}""")
    }
  }

}
