package skinny.controller

import skinny.engine.AsyncSkinnyEngineServlet

/**
 * SkinnyController as a Servlet.
 */
class AsyncSkinnyServlet
  extends AsyncSkinnyEngineServlet
  with AsyncSkinnyControllerBase
  with SkinnyWebPageControllerFeatures
