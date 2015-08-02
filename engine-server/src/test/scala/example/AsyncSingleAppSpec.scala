package example

import org.scalatest._
import skinny.engine._
import skinny.engine.json.EngineJSONStringOps
import skinny.engine.response.InternalServerError
import skinny.http.HTTP

import scala.concurrent._
import scala.util.control.NonFatal

class AsyncSingleAppSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  object Database {
    def findMessage(name: Option[String])(implicit ctx: ExecutionContext) = Future {
      if (name.isDefined && name.exists(_ == "Martin")) throw new RuntimeException
      else s"Hello, ${name.getOrElse("Anonymous")}"
    }
  }

  override def beforeAll() = {
    WebServer.init().mount(new AsyncSingleApp with EngineJSONStringOps {
      before() { implicit ctx =>
        contentType = "application/json; charset=utf-8"
      }

      get("/ok") { implicit ctx =>
        logger.info("params: " + params)
        "OK"
      }
      get("/json1") { implicit ctx =>
        Future {
          val name = params.get("name")
          if (name.isDefined && name.exists(_ == "Martin")) throw new RuntimeException
          else s"Hello, ${name.getOrElse("Anonymous")}"
        }.map { message =>
          responseAsJSON(Map("message" -> message)) // ServletConcurrencyException here
        }.recover {
          case NonFatal(e) =>
            InternalServerError(toJSONString(Map("message" -> ("Oops... " + e.getClass.getSimpleName))))
        }
      }
      get("/json2") { implicit ctx =>
        Database.findMessage(params.get("name")).map { message =>
          responseAsJSON(Map("message" -> message))
        }.recover {
          case NonFatal(e) =>
            logger.info(e.getMessage, e)
            InternalServerError(toJSONString(Map("message" -> ("Oops... " + e.getClass.getSimpleName))))
        }
      }
    }).port(8767).start()

    Thread.sleep(300)
  }

  it should "work" in {
    val response = HTTP.get("http://127.0.0.1:8767/ok")
    response.status should equal(200)
    response.textBody should equal("OK")
  }

  it should "not respond as NG when using Future without context" in {
    var failureFound = false
    var count = 0
    while (!failureFound && count < 30) {
      val response = HTTP.get("http://127.0.0.1:8767/json1")
      if (response.status != 500) {
        count += 1
        Thread.sleep(50)
      } else {
        failureFound = true
      }
    }
    failureFound should be(false)
  }

  it should "respond as 500 error with JSON body" in {
    val response = HTTP.get("http://127.0.0.1:8767/json1?name=Martin")
    response.status should equal(500)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
    response.textBody should equal("""{"message":"Oops... RuntimeException"}""")
  }

  it should "respond as OK with JSON body 2" in {
    val response = HTTP.get("http://127.0.0.1:8767/json2")
    response.status should equal(200)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
    response.textBody should equal("""{"message":"Hello, Anonymous"}""")
  }

  it should "respond as 500 error with JSON body 2" in {
    val response = HTTP.get("http://127.0.0.1:8767/json2?name=Martin")
    response.status should equal(500)
    response.header("Content-Type") should equal(Some("application/json; charset=utf-8"))
    response.textBody should equal("""{"message":"Oops... RuntimeException"}""")
  }

  override def afterAll() = {
    WebServer.stop()
  }

}
