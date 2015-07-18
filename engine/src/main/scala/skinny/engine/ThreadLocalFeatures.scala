package skinny.engine

import skinny.engine.base.{ MainThreadLocalEverywhere, BeforeAfterDsl }
import skinny.engine.json.JSONOperations
import skinny.engine.routing.RoutingDsl
import skinny.util.JSONStringOpsConfig

/**
 * Built-in features in SkinnyEngineFilter/SkinnyEngineServlet.
 * These traits should not be mixed in SkinnyEngineBase.
 */
trait ThreadLocalFeatures
    extends MainThreadLocalEverywhere
    with RoutingDsl
    with BeforeAfterDsl
    with JSONOperations
    with JSONStringOpsConfig { self: SkinnyEngineBase =>

}
