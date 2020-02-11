package skinny.routing.implicits

import scala.language.implicitConversions

import skinny.controller.feature.SkinnyControllerCommonBase

import skinny.micro.Handler
import skinny.micro.constant.{ Get, HttpMethod }
import skinny.micro.routing.Route

import skinny.routing.RichRoute

/**
  * Implicits for RichRoute.
  */
object RoutesAsImplicits extends RoutesAsImplicits

/**
  * Implicits for RichRoute which enables Route to call #as(String) method.
  */
trait RoutesAsImplicits {

  implicit def convertRouteToRichRoute(route: Route)(implicit controller: SkinnyControllerCommonBase): RichRoute = {
    val method =
      route.metadata.get(Handler.RouteMetadataHttpMethodCacheKey).map(_.asInstanceOf[HttpMethod]).getOrElse(Get)
    new RichRoute(route, method, controller)
  }

}
