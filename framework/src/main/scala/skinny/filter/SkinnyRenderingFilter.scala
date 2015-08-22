package skinny.filter

import skinny.controller.SkinnyWebPageControllerFeatures
import skinny.micro.ErrorHandler

/**
 * Skinny Rendering Filter.
 *
 * If you use Scatatra's filter (before/after not beforeAction/afterAction), be careful. It's pretty tricky.
 * Scalatra's filters would be applied for all the controllers defined below in ScalatraBootstrap.
 */
trait SkinnyRenderingFilter extends SkinnyFilter with SkinnyWebPageControllerFeatures {

  /**
   * Adds error handler which renders body to SkinnyController.
   *
   * @param handler
   */
  def addRenderingErrorFilter(handler: ErrorHandler) = {
    val mergedHandlers = skinnyErrorFilters.get(WithRendering).map(hs => hs :+ handler).getOrElse(Seq(handler))
    skinnyErrorFilters.update(WithRendering, mergedHandlers)
  }

}
