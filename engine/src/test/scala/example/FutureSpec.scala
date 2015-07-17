package example

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine.{ ServletConcurrencyException, SkinnyEngineServlet }
import skinny.engine.async.AsyncResult

import scala.concurrent.Future
import scala.concurrent.duration._

class FutureSpec extends ScalatraFlatSpec {

  addServlet(new SkinnyEngineServlet {

    get("/") {
      AsyncResult {
        responseAsJSON(params)
      }
    }

    get("/future") {
      futureWithContext { implicit ctx =>
        responseAsJSON(params(ctx))(ctx)
      }
    }

    get("/future-error") {
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
    get("/future-error?foo=bar") {
      status should equal(200)
      body.contains("Concurrency Issue Detected") should be(true)
    }
  }
  it should "work with futureWithContext" in {
    get("/future?foo=bar") {
      status should equal(200)
      body should equal("""{"foo":"bar"}""")
    }
  }

}
