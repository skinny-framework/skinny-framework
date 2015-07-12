package skinny.engine.routing

import skinny.engine.{ MultiParams, RouteTransformer }

/**
 * A route matcher is evaluated in the context it was created and returns a
 * a (possibly empty) multi-map of parameters if the route is deemed to match.
 */
trait RouteMatcher extends RouteTransformer {

  def apply(requestPath: String): Option[MultiParams]

  def apply(route: Route): Route = Route.appendMatcher(this)(route)

}