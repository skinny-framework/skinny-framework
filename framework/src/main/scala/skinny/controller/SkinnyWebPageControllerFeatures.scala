package skinny.controller

import skinny.controller.feature._

/**
  * Additional web pages specific features for SkinnyControllers.
  */
trait SkinnyWebPageControllerFeatures
    extends SkinnyControllerCommonBase
    with FlashFeature
    with TemplateEngineFeature
    with ScalateTemplateEngineFeature
    with CSRFProtectionFeature
    with XXSSProtectionHeaderFeature
    with XFrameOptionsHeaderFeature { self: BeforeAfterActionFeature =>

  override def handleForgeryIfDetected() = haltWithBody(403)(context)

}
