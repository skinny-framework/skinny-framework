package skinny.routing

import skinny.controller.{ ActionDefinition, Constants, SkinnyControllerBase }
import skinny.engine.constant.HttpMethod
import skinny.engine.routing.Route
import skinny.exception.RouteMetadataException

/**
 * Route wrapper.
 *
 * @param route route
 * @param method HTTP method
 * @param controller controller
 */
case class RichRoute(route: Route, method: HttpMethod, controller: SkinnyControllerBase) {

  /**
   * Registers action name to the controller.
   *
   * @param name action name
   * @return route
   */
  def as(name: Symbol): Route = {
    val expectedMethod = route.metadata.get(Constants.RouteMetadataHttpMethodCacheKey)
      .map(_.asInstanceOf[HttpMethod]).getOrElse {
        throw new RouteMetadataException(
          s"Metadata attribute ${Constants.RouteMetadataHttpMethodCacheKey} of Route should be set.")
      }
    controller.addActionDefinition(ActionDefinition(
      name = name,
      method = expectedMethod,
      matcher = (method: HttpMethod, path: String) => method == expectedMethod && route.apply(path).isDefined
    ))

    route
  }

}
