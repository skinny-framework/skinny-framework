package skinny.routing

import skinny.controller.ActionDefinition
import skinny.controller.feature.SkinnyControllerCommonBase
import skinny.exception.RouteMetadataException
import skinny.micro.Handler
import skinny.micro.constant.HttpMethod
import skinny.micro.routing.Route

/**
  * Route wrapper.
  *
  * @param route route
  * @param method HTTP method
  * @param controller controller
  */
case class RichRoute(route: Route, method: HttpMethod, controller: SkinnyControllerCommonBase) {

  /**
    * Registers action name to the controller.
    *
    * @param name action name
    * @return route
    */
  @deprecated(since = "4.0.0", message = "use as(String) instead.")
  def as(name: Symbol): Route = {
    val expectedMethod = route.metadata
      .get(Handler.RouteMetadataHttpMethodCacheKey)
      .map(_.asInstanceOf[HttpMethod])
      .getOrElse {
        throw new RouteMetadataException(
          s"Metadata attribute ${Handler.RouteMetadataHttpMethodCacheKey} of Route should be set."
        )
      }
    controller.addActionDefinition(
      ActionDefinition(
        name = name.name,
        method = expectedMethod,
        matcher = (method: HttpMethod, path: String) => method == expectedMethod && route.apply(path).isDefined
      )
    )

    route
  }

  def as(name: String): Route = as(Symbol(name))

}
