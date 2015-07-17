package skinny.controller

import skinny.engine.AsyncSkinnyEngineFilter

/**
 * SkinnyController as a Servlet for REST APIs.
 *
 * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
 */
trait AsyncSkinnyApiController
  extends AsyncSkinnyControllerBase
  with AsyncSkinnyEngineFilter