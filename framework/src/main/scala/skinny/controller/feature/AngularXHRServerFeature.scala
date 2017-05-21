package skinny.controller.feature

import skinny.micro.SkinnyMicroBase

/**
  * Server side implementation for Angular apps.
  */
trait AngularXHRServerFeature
    extends AngularJSONFeature
    with JSONParamsAutoBinderFeature
    with AngularXSRFProtectionFeature {

  self: SkinnyMicroBase with ActionDefinitionFeature with BeforeAfterActionFeature with RequestScopeFeature =>

}
