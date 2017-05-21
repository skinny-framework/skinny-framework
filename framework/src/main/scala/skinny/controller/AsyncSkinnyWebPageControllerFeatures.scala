package skinny.controller

import skinny.controller.feature._

/**
  * Additional web pages specific features for SkinnyControllers.
  */
trait AsyncSkinnyWebPageControllerFeatures
    extends SkinnyControllerCommonBase
    with FlashFeature
    with TemplateEngineFeature
    with ScalateTemplateEngineFeature
    with AsyncCSRFProtectionFeature
    with AsyncXXSSProtectionHeaderFeature
    with AsyncXFrameOptionsHeaderFeature { self: AsyncBeforeAfterActionFeature =>

  override def handleForgeryIfDetected() = haltWithBody(403)(context)

}
