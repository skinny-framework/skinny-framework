package skinny.controller

/**
 * Skinny Servlet Controller for REST APIs.
 *
 * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
 */
trait SkinnyApiServlet
  extends org.scalatra.ScalatraServlet
  with SkinnyControllerBase
