package skinny.controller.feature

import skinny.engine.SkinnyEngineBase
import skinny.engine.json.JSONOperations
import skinny.util.AngularJSONStringOps

/**
 * Angular application's server side API support.
 */
trait AngularJSONFeature
    extends JSONOperations
    with AngularJSONStringOps { self: SkinnyEngineBase =>

}
