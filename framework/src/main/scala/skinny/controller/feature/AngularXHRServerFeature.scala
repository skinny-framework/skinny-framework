package skinny.controller.feature

import skinny.Logging
import skinny.engine.SkinnyScalatraBase

/**
 * Server side implementation for Angular apps.
 */
trait AngularXHRServerFeature
    extends AngularJSONFeature
    with JSONParamsAutoBinderFeature
    with AngularXSRFProtectionFeature {

  self: SkinnyScalatraBase with ActionDefinitionFeature with BeforeAfterActionFeature with RequestScopeFeature with Logging =>

}