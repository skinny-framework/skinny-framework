package skinny.engine.base

import skinny.engine.routing.RouteRegistry

trait RouteRegistryAccessor {

  /**
   * The routes registered in this kernel.
   */
  lazy val routes: RouteRegistry = new RouteRegistry

}
