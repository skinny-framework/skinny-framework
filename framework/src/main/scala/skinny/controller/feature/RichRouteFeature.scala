package skinny.controller.feature

import skinny.controller.Constants
import org.scalatra._

/**
 * RichRoute support.
 */
trait RichRouteFeature extends ScalatraBase {

  /**
   * Override to append HTTP method information to Route objects.
   */
  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val route = super.addRoute(method, transformers, action)
    route.copy(metadata = route.metadata.updated(Constants.RouteMetadataHttpMethodCacheKey, method))
  }

}
