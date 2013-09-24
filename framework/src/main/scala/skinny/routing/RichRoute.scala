package skinny.routing

import org.scalatra._
import skinny.controller.{ ActionDefinition, Constants, SkinnyControllerBase }
import skinny.exception.RouteMetadataException

case class RichRoute(route: Route, method: HttpMethod, controller: SkinnyControllerBase) {

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
