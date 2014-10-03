package skinny.controller.feature

import org.scalatra.SkinnyScalatraBase
import skinny.Logging

/**
 * Server side implementation for Angular apps.
 */
trait AngularXHRServerFeature
    extends AngularJSONFeature
    with JSONParamsAutoBinderFeature
    with AngularXSRFProtectionFeature {

  self: SkinnyScalatraBase with ActionDefinitionFeature with BeforeAfterActionFeature with RequestScopeFeature with Logging =>

}