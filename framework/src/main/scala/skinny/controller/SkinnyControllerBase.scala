package skinny.controller

import org.scalatra._
import skinny.controller.feature._
import grizzled.slf4j.Logging
import skinny.validator.implicits.ParamsGetAsImplicits
import skinny.controller.implicits.ParamsPermitImplicits
import skinny.routing.implicits.RoutesAsImplicits

trait SkinnyControllerBase extends ScalatraBase
  with BasicFeature
  with RequestScopeFeature
  with BeforeAfterActionFeature
  with SessionLocaleFeature
  with FlashFeature
  with ValidationFeature
  with TemplateEngineFeature
  with ScalateTemplateEngineFeature
  with CSRFProtectionFeature
  with RoutesAsImplicits
  with ParamsGetAsImplicits
  with ParamsPermitImplicits
  with Logging
