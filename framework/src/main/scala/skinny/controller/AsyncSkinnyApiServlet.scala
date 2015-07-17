package skinny.controller

import skinny.engine.AsyncSkinnyEngineServlet

/**
 * Skinny Servlet Controller for REST APIs.
 *
 * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
 */
trait AsyncSkinnyApiServlet
  extends AsyncSkinnyEngineServlet
  with AsyncSkinnyControllerBase
