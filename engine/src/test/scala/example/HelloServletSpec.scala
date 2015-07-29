package example

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine._

import scala.concurrent.Future

object HelloServlet extends SingleApp {

  def message(implicit ctx: Context) = {
    s"Hello, ${params(ctx).getOrElse("name", "Anonymous")}"
  }

  // synchronous action
  get("/hello")(message)
  post("/hello")(message)

  // asynchronous action
  get("/hello/async") {
    implicit val ctx = context
    Future { message(ctx) }
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

  it should "work fine with GET Requests" in {
    get("/hello") {
      status should equal(200)
      body should equal("Hello, Anonymous")
    }
    get("/hello?name=Martin") {
      status should equal(200)
      body should equal("Hello, Martin")
    }
  }

  it should "work fine with POST Requests" in {
    post("/hello", Map()) {
      status should equal(200)
      body should equal("Hello, Anonymous")
    }
    post("/hello", Map("name" -> "Martin")) {
      status should equal(200)
      body should equal("Hello, Martin")
    }
  }

  it should "work fine with AsyncResult" in {
    get("/hello/async") {
      status should equal(200)
      body should equal("Hello, Anonymous")
    }
    get("/hello/async?name=Martin") {
      status should equal(200)
      body should equal("Hello, Martin")
    }
  }

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
