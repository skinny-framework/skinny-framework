package skinny.routing.implicits

import scala.language.implicitConversions

import org.scalatra._
import skinny.controller._
import skinny.routing.RichRoute

object RoutesAsImplicits extends RoutesAsImplicits

trait RoutesAsImplicits {

  implicit def convertRouteToRichRoute(route: Route)(implicit controller: SkinnyControllerBase): RichRoute = {
    val method = route.metadata.get(Constants.RouteMetadataHttpMethodCacheKey).map(_.asInstanceOf[HttpMethod]).getOrElse(Get)
    new RichRoute(route, method, controller)
  }

}
