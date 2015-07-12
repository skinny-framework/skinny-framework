package skinny.controller

import skinny.engine.SkinnyEngineServlet

/**
 * SkinnyController as a Servlet.
 */
class SkinnyServlet
  extends SkinnyEngineServlet
  with SkinnyControllerBase
  with SkinnyWebPageControllerFeatures
