package skinny.controller

import org.scalatra.ScalatraFilter

/**
 * SkinnyController as a Servlet for REST APIs.
 *
 * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
 */
trait SkinnyApiController
  extends SkinnyControllerBase
  with ScalatraFilter