package skinny.controller

import skinny.controller.feature.{ CSRFProtectionFeature, ScalateTemplateEngineFeature, TemplateEngineFeature, FlashFeature }

/**
 * Additional web pages specific features for SkinnyControllers.
 */
trait SkinnyWebPageControllerFeatures
  extends SkinnyControllerBase
  with FlashFeature
  with TemplateEngineFeature
  with ScalateTemplateEngineFeature
  with CSRFProtectionFeature