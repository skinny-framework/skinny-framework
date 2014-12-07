package skinny

import org.scalatest._

class FormatSpec extends FlatSpec with Matchers {
  behavior of "Format"

  "HTML" should "be available" in {
    Format.HTML.name should equal("html")
    Format.HTML.contentType should equal("text/html")
  }

  "JSON" should "be available" in {
    Format.JSON.name should equal("json")
    Format.JSON.contentType should equal("application/json")
  }

  "XML" should "be available" in {
    Format.XML.name should equal("xml")
    Format.XML.contentType should equal("application/xml")
  }

  "JavaScript" should "be available" in {
    Format.JavaScript.name should equal("js")
    Format.JavaScript.contentType should equal("application/javascript")
  }

}
