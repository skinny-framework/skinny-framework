package skinny.controller.feature

import skinny.engine.SkinnyEngineBase

/**
 * Server side implementation for Angular apps.
 */
trait AngularXHRServerFeature
    extends AngularJSONFeature
    with JSONParamsAutoBinderFeature
    with AngularXSRFProtectionFeature {

  self: SkinnyEngineBase with ActionDefinitionFeature with BeforeAfterActionFeature with RequestScopeFeature =>

}