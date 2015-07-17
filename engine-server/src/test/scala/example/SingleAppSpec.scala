package example

import org.scalatest._
import skinny.engine._
import skinny.engine.response.InternalServerError
import skinny.http.HTTP

import scala.concurrent._
import scala.util.control.NonFatal

class SingleAppSpec extends FlatSpec with Matchers with BeforeAndAfter {

  object Database {
    def findMessage(name: Option[String])(implicit ctx: ExecutionContext) = Future {
      if (name.isDefined && name.exists(_ == "Martin")) throw new RuntimeException
      else s"Hello, ${name.getOrElse("Anonymous")}"
    }
  }

  before {
    WebServer.mount(new SingleApp {
      before() {
        contentType = "application/json; charset=utf-8"
      }

      get("/ok") {
        logger.info("params: " + params)
        "OK"
      }
      get("/json1") {
        val name = params.getAs[String]("name")
        Future {
          if (name.isDefined && name.exists(_ == "Martin")) throw new RuntimeException
          else s"Hello, ${name.getOrElse("Anonymous")}"
        }.map { message =>
          responseAsJSON(Map("message" -> message)) // ServletConcurrencyException here
        }.recover {
          case NonFatal(e) => InternalServerError(toJSONString(Map("message" -> ("Oops... " + e.getClass.getSimpleName))))
        }
      }
      get("/json2") {
        implicit val ctx = context
        Database.findMessage(params(ctx).getAs[String]("name")).map { message =>
          responseAsJSON(Map("message" -> message))(ctx)
        }.recover {
          case NonFatal(e) =>
            logger.info(e.getMessage, e)
            InternalServerError(toJSONString(Map("message" -> ("Oops... " + e.getClass.getSimpleName))))
        }
      }
    }).port(8766).start()
    Thread.sleep(100)
  }

  it should "work" in {
    val response = HTTP.get("http://127.0.0.1:8766/ok")
    response.status should equal(200)
    response.textBody should equal("OK")
  }

  it should "respond as OK with JSON body" in {
    val response = HTTP.get("http://127.0.0.1:8766/json1")
    response.textBody should equal("""{"message":"Oops... ServletConcurrencyException"}""")
    response.status should equal(500)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
  }

  it should "respond as 500 error with JSON body" in {
    val response = HTTP.get("http://127.0.0.1:8766/json1?name=Martin")
    response.status should equal(500)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
    response.textBody should equal("""{"message":"Oops... RuntimeException"}""")
  }

  it should "respond as OK with JSON body 2" in {
    val response = HTTP.get("http://127.0.0.1:8766/json2")
    response.status should equal(200)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
    response.textBody should equal("""{"message":"Hello, Anonymous"}""")
  }

  it should "respond as 500 error with JSON body 2" in {
    val response = HTTP.get("http://127.0.0.1:8766/json2?name=Martin")
    response.status should equal(500)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
    response.textBody should equal("""{"message":"Oops... RuntimeException"}""")
  }

  after {
    WebServer.stop()
  }

}
