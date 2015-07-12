package skinny.routing.implicits

import skinny.engine.constant.{ Get, HttpMethod }
import skinny.engine.routing.Route

import scala.language.implicitConversions

import skinny.controller._
import skinny.routing.RichRoute

/**
 * Implicits for RichRoute.
 */
object RoutesAsImplicits extends RoutesAsImplicits

/**
 * Implicits for RichRoute which enables Route to call #as(Symbol) method.
 */
trait RoutesAsImplicits {

  implicit def convertRouteToRichRoute(route: Route)(implicit controller: SkinnyControllerBase): RichRoute = {
    val method = route.metadata.get(Constants.RouteMetadataHttpMethodCacheKey).map(_.asInstanceOf[HttpMethod]).getOrElse(Get)
    new RichRoute(route, method, controller)
  }

}
