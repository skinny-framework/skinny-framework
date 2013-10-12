package skinny.controller

import skinny.controller.feature._
import skinny.validator.implicits.ParamsGetAsImplicits
import skinny.controller.implicits.ParamsPermitImplicits
import skinny.routing.implicits.RoutesAsImplicits

trait SkinnyControllerBase
  extends org.scalatra.ScalatraBase
  with EnvFeature
  with RichRouteFeature
  with RequestScopeFeature
  with ActionDefinitionFeature
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
  with grizzled.slf4j.Logging
