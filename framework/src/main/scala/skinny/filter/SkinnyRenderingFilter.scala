package skinny.filter

import skinny.controller.SkinnyWebPageControllerFeatures
import skinny.controller.feature.BeforeAfterActionFeature
import skinny.micro.ErrorHandler

/**
  * Skinny Rendering Filter.
  *
  * If you use Skinny Micro's filter (before/after not beforeAction/afterAction), be careful. It's pretty tricky.
  * Skinny Micro's filters would be applied for all the controllers defined below in Bootstrap.
  */
trait SkinnyRenderingFilter extends SkinnyFilter with BeforeAfterActionFeature with SkinnyWebPageControllerFeatures {

  /**
    * Adds error handler which renders body to SkinnyController.
    */
  def addRenderingErrorFilter(handler: ErrorHandler) = {
    val mergedHandlers = skinnyErrorFilters.get(WithRendering).map(hs => hs :+ handler).getOrElse(Seq(handler))
    skinnyErrorFilters.update(WithRendering, mergedHandlers)
  }

}
