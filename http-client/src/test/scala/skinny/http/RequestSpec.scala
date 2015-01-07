package skinny.http

import org.specs2.mutable.Specification

class RequestSpec extends Specification {

  sequential

  val request = new Request("http://example.com/")

  "Request" should {
    "be available" in {
      request.enableThrowingIOException(true)
      request.url("http://www.example.com")
      request.followRedirects(true)
      request.connectTimeoutMillis(100)
      request.readTimeoutMillis(100)
      request.referer("foo")
      request.userAgent("ua")
      request.contentType("text/html")
      request.header("foo") should equalTo(None)
      request.header("foo", "bar")
      request.headerNames.size should equalTo(1)
    }
  }

}
