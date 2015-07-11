package org.scalatra.test

import java.io.{ InputStream, OutputStream }
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

import org.scalatest._

import scala.annotation.tailrec
import scala.collection.JavaConversions._

class HttpComponentsClientSpec
    extends WordSpec
    with Matchers
    with BeforeAndAfter
    with HttpComponentsClient
    with EmbeddedJettyContainer {

  before { start() }
  after { stop() }

  private val servlet = new HttpServlet {
    override def service(req: HttpServletRequest, resp: HttpServletResponse) {
      def copy(in: InputStream, out: OutputStream, bufferSize: Int = 4096) {
        val buf = new Array[Byte](bufferSize)
        @tailrec
        def loop() {
          val n = in.read(buf)
          if (n >= 0) {
            out.write(buf, 0, n)
            loop()
          }
        }
        loop()
      }

      resp.setHeader("Request-Method", req.getMethod.toUpperCase)
      resp.setHeader("Request-URI", req.getRequestURI)
      req.getHeaderNames.foreach(headerName =>
        resp.setHeader("Request-Header-%s".format(headerName), req.getHeader(headerName)))

      req.getParameterMap.foreach {
        case (name, values) =>
          resp.setHeader("Request-Param-%s".format(name), values.mkString(", "))
      }

      resp.getOutputStream.write("received: ".getBytes)
      copy(req.getInputStream, resp.getOutputStream)
    }
  }
  addServlet(servlet, "/*")

  "client" should {
    "support all HTTP methods" in {
      doVerbGetActual("PUT") should equal("PUT")
      doVerbGetActual("POST") should equal("POST")
      doVerbGetActual("TRACE") should equal("TRACE")
      doVerbGetActual("GET") should equal("GET")
      doVerbGetActual("HEAD") should equal("HEAD")
      doVerbGetActual("OPTIONS") should equal("OPTIONS")
      doVerbGetActual("DELETE") should equal("DELETE")
      doVerbGetActual("PATCH") should equal("PATCH")
    }

    "submit query string parameters" in {
      get("/", Map("param1" -> "value1", "param2" -> "value2")) {
        header("Request-Param-param1") should equal("value1")
        header("Request-Param-param2") should equal("value2")
      }
    }

    "submit headers" in {
      get("/", headers = Map("X-Hello-Server" -> "hello")) {
        header("Request-Header-X-Hello-Server") should equal("hello")
      }
    }

    "submit body for POST/PUT/PATCH requests" in {
      doReqWithBody("POST", "post test") should equal("received: post test")
      doReqWithBody("PUT", "put test") should equal("received: put test")
      doReqWithBody("PATCH", "patch test") should equal("received: patch test")
    }
  }

  private def doVerbGetActual(method: String) = {
    submit(method, "/") {
      header("Request-Method")
    }
  }

  private def doReqWithBody(method: String, reqBody: String) = {
    submit(method, "/", body = reqBody) {
      body
    }
  }

}
