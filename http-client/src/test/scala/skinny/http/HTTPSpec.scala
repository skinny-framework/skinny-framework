package skinny.http

import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration._
import java.io.File

class HTTPSpec extends Specification with Handlers with ServerOps {

  sequential

  override def intToRichLong(v: Int) = super.intToRichLong(v)

  "HTTP" should {

    // --------
    // GET

    "get" in {
      withServer(8077) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = HTTP.get("http://localhost:8077/?foo=bar")
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get with queryParams" in {
      withServer(8177) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = HTTP.get("http://localhost:8177/", "foo" -> "bar")
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get using queryParams method" in {
      withServer(8277) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = HTTP.get(Request("http://localhost:8277/").queryParams("foo" -> "bar"))
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get using queryParam method" in {
      withServer(8278) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = HTTP.get(Request("http://localhost:8278/").queryParam("foo" -> "bar").queryParam("bar" -> "baz"))
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar,bar:baz")
      }
    }

    "get with charset" in {
      withServer(8377) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = HTTP.get("http://localhost:8377/?foo=bar", "UTF-8")
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get asynchronously" in {
      withServer(8077) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = Await.result(HTTP.asyncGet("http://localhost:8077/?foo=bar"), 5.seconds)
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get asynchronously with queryParams" in {
      withServer(8177) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = Await.result(HTTP.asyncGet("http://localhost:8177/", "foo" -> "bar"), 5.seconds)
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get asynchronously using queryParams method" in {
      withServer(8278) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = Await.result(HTTP.asyncGet(Request("http://localhost:8278/").queryParams("foo" -> "bar")), 5.seconds)
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    "get asynchronously with charset" in {
      withServer(8377) { server =>
        server.setHandler(getHandler)
        start(server)

        val response = Await.result(HTTP.asyncGet("http://localhost:8377/?foo=bar", "UTF-8"), 5.seconds)
        response.status must equalTo(200)
        response.asString.length must be_>(0)
        response.asString must equalTo("foo:bar")
      }
    }

    // --------
    // POST

    "post with data string" in {
      withServer(8187) { server =>
        server.setHandler(postHandler)
        start(server)

        val response = HTTP.post("http://localhost:8187/", "foo=bar")
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "post with Map" in {
      withServer(8287) { server =>
        server.setHandler(postHandler)
        start(server)

        val response = HTTP.post("http://localhost:8287/", "foo" -> "bar")
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "post with TextInput" in {
      withServer(new _root_.server.PostFormdataServer(8888)) { server =>
        start(server)

        val response = HTTP.postMultipart("http://localhost:8888/", FormData("toResponse", TextInput("bar")))
        response.status must equalTo(200)
        response.asString must equalTo("bar")
      }
    }

    "post with FileInput" in {
      withServer(new _root_.server.PostFormdataServer(8888)) { server =>
        start(server)

        val file = new File("http-client/src/test/resources/sample.txt")
        file.exists must equalTo(true)

        val response = HTTP.postMultipart("http://localhost:8888/", FormData("toResponse", FileInput(file, "text/plain")))
        response.status must equalTo(200)
        response.asString must equalTo(
          """foo
            |bar
            |バズ""".stripMargin
        )
      }
    }

    "post asynchronously with data string" in {
      withServer(newServer(8187)) { server =>
        server.setHandler(postHandler)
        start(server)

        val response = Await.result(HTTP.asyncPost("http://localhost:8187/", "foo=bar"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "post asynchronously with Map" in {
      withServer(newServer(8287)) { server =>
        server.setHandler(postHandler)
        start(server)

        val response = Await.result(HTTP.asyncPost("http://localhost:8287/", "foo" -> "bar"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "post asynchronously with TextInput" in {
      withServer(new _root_.server.PostFormdataServer(8888)) { server =>
        start(server)

        val response = Await.result(HTTP.asyncPostMultipart("http://localhost:8888/", FormData("toResponse", TextInput("bar"))), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("bar")
      }
    }

    // --------
    // PUT

    "put with data string" in {
      withServer(newServer(8186)) { server =>
        server.setHandler(putHandler)
        start(server)

        val response = HTTP.put("http://localhost:8186/", "foo=bar")
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "put with data Map" in {
      withServer(newServer(8285)) { server =>
        server.setHandler(putHandler)
        start(server)

        val response = HTTP.put("http://localhost:8285/", "foo" -> "bar")
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "put asynchronously with data string" in {
      withServer(newServer(8186)) { server =>
        server.setHandler(putHandler)
        start(server)

        val response = Await.result(HTTP.asyncPut("http://localhost:8186/", "foo=bar"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    "put asynchronously with data Map" in {
      withServer(newServer(8286)) { server =>
        server.setHandler(putHandler)
        start(server)

        val response = Await.result(HTTP.asyncPut("http://localhost:8286/", "foo" -> "bar"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("foo:bar")
      }
    }

    // --------
    // DELETE

    "delete" in {
      withServer(8485) { server =>
        server.setHandler(deleteHandler)
        start(server)

        val response = HTTP.delete("http://localhost:8485/resource")
        response.status must equalTo(200)
        response.asString must equalTo("")
      }
    }

    "delete asynchronously" in {
      withServer(8486) { server =>
        server.setHandler(deleteHandler)
        start(server)

        val response = Await.result(HTTP.asyncDelete("http://localhost:8486/resource"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("")
      }
    }

    // --------
    // TRACE

    "trace" in {
      withServer(8585) { server =>
        server.setHandler(traceHandler)
        start(server)

        val response = HTTP.trace("http://localhost:8585/resource")
        response.status must equalTo(200)
        response.asString must equalTo("")
      }
    }

    "trace asynchronously" in {
      withServer(8586) { server =>
        server.setHandler(traceHandler)
        start(server)

        val response = Await.result(HTTP.asyncTrace("http://localhost:8586/resource"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("")
      }
    }

    // --------
    // OPTIONS

    "options" in {
      withServer(8685) { server =>
        server.setHandler(optionsHandler)
        start(server)

        val response = HTTP.options("http://localhost:8685/resource")
        response.status must equalTo(200)
        response.asString must equalTo("")
      }
    }

    "options asynchronously" in {
      withServer(8686) { server =>
        server.setHandler(optionsHandler)
        start(server)

        val response = Await.result(HTTP.asyncOptions("http://localhost:8686/resource"), 5.seconds)
        response.status must equalTo(200)
        response.asString must equalTo("")
      }
    }

  }

}
