package skinny.controller.feature

import skinny.engine.SkinnyEngineBase
import skinny.engine.json.EngineJSONStringOps
import skinny.json.AngularJSONStringOps

/**
 * Angular application's server side API support.
 */
trait AngularJSONFeature
    extends EngineJSONStringOps
    with AngularJSONStringOps { self: SkinnyEngineBase =>

}
