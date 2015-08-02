package skinny.engine.base

import skinny.engine.RouteTransformer
import skinny.engine.routing.Route

/**
 * Before/After DSL for non-async skinny-engine.
 */
trait BeforeAfterDsl { self: RouteRegistryAccessor =>

  /**
   * Adds a filter to run before the route.  The filter only runs if each
   * routeMatcher returns Some.  If the routeMatchers list is empty, the
   * filter runs for all routes.
   */
  def before(transformers: RouteTransformer*)(fun: => Any): Unit = {
    routes.appendBeforeFilter(Route(transformers, () => fun))
  }

  /**
   * Adds a filter to run after the route.  The filter only runs if each
   * routeMatcher returns Some.  If the routeMatchers list is empty, the
   * filter runs for all routes.
   */
  def after(transformers: RouteTransformer*)(fun: => Any): Unit = {
    routes.appendAfterFilter(Route(transformers, () => fun))
  }

}
