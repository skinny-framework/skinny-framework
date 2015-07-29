package example.scalate

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny.engine._
import skinny.engine.scalate.ScalateSupport

object Hello extends WebApp with ScalateSupport {
  error {
    case e: Exception => e.printStackTrace()
  }

  get("/hello/scalate") {
    contentType = "text/html"
    ssp("/index", "name" -> "foo")
  }
}

class HelloSpec extends ScalatraFlatSpec {
  addFilter(Hello, "/*")

  it should "work fine with scalate" in {
    get("/hello/scalate") {
      status should equal(200)
      body should equal("<div>Hello, foo</div>\n")
    }
  }
}
