package skinny.controller

import skinny.controller.feature.FileUploadFeature

/**
 * SkinnyController as a Servlet.
 */
class SkinnyServlet
  extends org.scalatra.ScalatraServlet
  with SkinnyControllerBase
  with SkinnyWebPageControllerFeatures
