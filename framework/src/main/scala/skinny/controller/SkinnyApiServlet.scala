package skinny.controller

import skinny.engine.SkinnyEngineServlet

/**
 * Skinny Servlet Controller for REST APIs.
 *
 * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
 */
trait SkinnyApiServlet
  extends SkinnyEngineServlet
  with SkinnyControllerBase
