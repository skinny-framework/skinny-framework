package example

import org.scalatest._
import skinny.engine._
import skinny.engine.response.InternalServerError
import skinny.http.HTTP

import scala.concurrent._
import scala.util.control.NonFatal

class SingleAppSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  object Database {
    def findMessage(name: Option[String])(implicit ctx: ExecutionContext) = Future {
      if (name.isDefined && name.exists(_ == "Martin")) throw new RuntimeException
      else s"Hello, ${name.getOrElse("Anonymous")}"
    }
  }

  override def beforeAll() = {
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

    Thread.sleep(300)
  }

  it should "work" in {
    val response = HTTP.get("http://127.0.0.1:8766/ok")
    response.status should equal(200)
    response.textBody should equal("OK")
  }

  // NOTE: this behavior doesn't always happen
  it should "respond as NG when using Future without context" in {
    var failureFound = false
    var count = 0
    while (!failureFound && count < 30) {
      val response = HTTP.get("http://127.0.0.1:8766/json1")
      if (response.status != 500) {
        count += 1
        Thread.sleep(50)
      } else {
        failureFound = true
        response.status should equal(500)
        response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
        response.textBody should equal("""{"message":"Oops... ServletConcurrencyException"}""")
      }
    }
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

  override def afterAll() = {
    WebServer.stop()
  }

}
