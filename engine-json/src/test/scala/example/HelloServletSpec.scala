package example

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine._
import skinny.engine.json._

import scala.concurrent.Future

object HelloServlet extends SingleApp with EngineJSONStringOps {

  def message(implicit ctx: Context) = {
    s"Hello, ${params(ctx).getOrElse("name", "Anonymous")}"
  }

  // returns JSON response
  get("/hello/json") {
    responseAsJSON(Map("message" -> message))
  }
  get("/hello/json/async") {
    implicit val ctx = context
    Future {
      responseAsJSON(Map("message" -> s"Hello, ${params(ctx).getOrElse("name", "Anonymous")}"))(ctx)
    }
  }
}

class HelloServletSpec extends ScalatraFlatSpec {
  addServlet(HelloServlet, "/*")

  it should "return JSON response" in {
    get("/hello/json") {
      status should equal(200)
      header("Content-Type") should equal("application/json; charset=utf-8")
      body should equal("""{"message":"Hello, Anonymous"}""")
    }
    get("/hello/json/async?name=Martin") {
      status should equal(200)
      header("Content-Type") should equal("application/json; charset=utf-8")
      body should equal("""{"message":"Hello, Martin"}""")
    }
  }
}
