package skinny.controller.feature

import skinny.engine.SkinnyEngineBase
import skinny.util.AngularJSONStringOps

/**
 * Angular application's server side API support.
 */
trait AngularJSONFeature extends JSONFeature with AngularJSONStringOps { self: SkinnyEngineBase =>

}
