package skinny.test

import java.util.Locale

import org.scalatest._

class MockHttpServletResponseSpec extends FlatSpec with Matchers {

  it should "be available" in {
    val resp = new MockHttpServletResponse

    resp.getLocale should equal(Locale.getDefault())
    resp.setLocale(Locale.getDefault)
    resp.reset()
    resp.isCommitted should equal(false)
    resp.resetBuffer()
    resp.flushBuffer()
    resp.getBufferSize should equal(4096)
    resp.setBufferSize(1024)
    resp.setContentType("text/html")
    resp.setContentLength(123)
    resp.setCharacterEncoding("utf-8")
    resp.getWriter
    resp.getOutputStream should not equal (null)
    resp.getContentType should equal("text/html")
    resp.getCharacterEncoding should equal("utf-8")
    resp.getStatus should equal(200)
    resp.setStatus(201)
    resp.setStatus(201, "created")
    resp.getHeaderNames.size should equal(0)
    resp.getHeaders("foo").size should equal(0)
    resp.getHeader("foo") should equal(null)
    resp.addHeader("foo", "bar")
    resp.setHeader("foo", "baz")
    intercept[UnsupportedOperationException] {
      resp.addIntHeader("foo", 123)
    }
    resp.setIntHeader("foo", 123)
    resp.setDateHeader("foo", new java.util.Date().getTime)
    intercept[UnsupportedOperationException] {
      resp.addDateHeader("foo", new java.util.Date().getTime)
    }
    resp.containsHeader("foo") should equal(true)
    resp.sendRedirect("/foo")
    resp.sendError(302)
    resp.sendError(302, "foo")
    resp.encodeRedirectUrl("/foo") should equal("/foo")
    resp.encodeRedirectURL("/foo") should equal("/foo")
    resp.encodeUrl("/foo") should equal("/foo")
    resp.encodeURL("/foo") should equal("/foo")
    resp.addCookie(null)
    resp.setContentLengthLong(123L)
  }

}
