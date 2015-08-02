package skinny.engine.base

import skinny.engine.routing.RouteRegistry

/**
 * RouteRegistry.
 */
trait RouteRegistryAccessor {

  /**
   * The routes registered in this kernel.
   */
  protected val routes: RouteRegistry = new RouteRegistry

}
