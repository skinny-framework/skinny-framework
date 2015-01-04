package skinny.test

import javax.servlet.http.HttpUpgradeHandler

import org.scalatest._

class MockHttpServletRequestSpec extends FlatSpec with Matchers {

  it should "be available" in {
    val req = new MockHttpServletRequest

    req.getDispatcherType should equal(null)
    req.getAsyncContext should equal(null)
    req.isAsyncSupported should equal(true)
    req.isAsyncStarted should equal(false)
    req.startAsync(null, null)
    req.startAsync

    req.getServletContext should not equal (null)
    req.getRealPath("/") should equal(null)

    req.getLocalPort should equal(80)
    req.getLocalAddr should equal("127.0.0.1")
    req.getLocalName should equal("localhost")

    req.getServerPort should equal(80)
    req.getServerName should equal("localhost")

    req.getRemotePort should equal(80)
    req.getRemoteAddr should equal("127.0.0.1")
    req.getRemoteHost should equal("localhost")

    req.getRequestDispatcher("/") should not equal (null)

    req.isSecure should equal(false)
    req.getLocales.hasMoreElements should equal(false)
    req.getLocale should equal(null)
    req.getAttributeNames.hasMoreElements should equal(false)
    req.removeAttribute("foo")

    req.getReader should not equal (null)
    req.getInputStream should not equal (null)

    req.getScheme should equal("http")
    req.getProtocol should equal("http")

    req.getParameterValues("foo").size should equal(0)
    req.getParameterNames should not equal (null)
    Option(req.getParameter("foo")) should equal(None)

    req.getContentType should equal(null)
    req.getContentLength should equal(-1)
    req.setCharacterEncoding("test")
    req.updateContentTypeHeader()

    req.doAddHeaderValue("foo", "bar", true)
    req.doAddHeaderValue("foo", "bar", false)

    req.getCharacterEncoding should equal("test")

    req.getPart("foo") should equal(null)
    req.getParts.size() should equal(0)

    req.logout()

    intercept[UnsupportedOperationException] {
      req.login("foo", "bar")
    }
    intercept[UnsupportedOperationException] {
      req.authenticate(null)
    }

    req.isRequestedSessionIdFromUrl should equal(false)
    req.isRequestedSessionIdFromURL should equal(false)

    req.isRequestedSessionIdFromCookie should equal(true)
    req.isRequestedSessionIdValid should equal(true)

    req.getSession should not equal (null)
    req.getSession(true) should not equal (null)

    req.getServletPath should equal(null)

    req.requestURI = "/foo"
    req.getRequestURL.toString should equal("http://localhost/foo")

    req.getRequestedSessionId should equal(null)
    req.getUserPrincipal should equal(null)
    req.isUserInRole(null) should equal(false)

    req.remoteUser = "user"
    req.getRemoteUser should equal("user")

    req.getContextPath should equal("")
    req.getPathInfo should equal(null)

    req.getMethod should equal(null)

    req.getHeaderNames should not equal (null)
    req.getHeader("foo") should equal("bar")

    req.getCookies.size should equal(0)

    req.authType = "at"
    req.getAuthType should equal("at")

    intercept[UnsupportedOperationException] {
      req.changeSessionId()
    }
    intercept[UnsupportedOperationException] {
      req.upgrade(classOf[HttpUpgradeHandler])
    }

    req.getContentLengthLong should equal(-1)
  }

}
