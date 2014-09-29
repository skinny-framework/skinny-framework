package skinny.controller

import skinny.controller.feature._

/**
 * Additional web pages specific features for SkinnyControllers.
 */
trait SkinnyWebPageControllerFeatures
    extends SkinnyControllerBase
    with FlashFeature
    with TemplateEngineFeature
    with ScalateTemplateEngineFeature
    with CSRFProtectionFeature
    with XXSSProtectionHeaderFeature
    with XFrameOptionsHeaderFeature {

  override def handleForgeryIfDetected() = haltWithBody(403)

}