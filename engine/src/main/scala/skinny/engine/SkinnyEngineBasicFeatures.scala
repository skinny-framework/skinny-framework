package skinny.engine

import skinny.engine.base.BeforeAfterDsl
import skinny.engine.json.JSONOperations

/**
 * Built-in features in SkinnyEngineFilter/SkinnyEngineServlet.
 * These traits should not be mixed in SkinnyEngineBase.
 */
trait SkinnyEngineBasicFeatures
    extends BeforeAfterDsl
    with JSONOperations { self: SkinnyEngineBase =>

}
