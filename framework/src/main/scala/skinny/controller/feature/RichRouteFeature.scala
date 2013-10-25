package skinny.controller.feature

import skinny.controller.Constants
import org.scalatra._
import skinny._

/**
 * RichRoute support.
 */
trait RichRouteFeature extends ScalatraBase { self: SkinnyControllerBase =>

  /**
   * Override to append HTTP method information to Route objects.
   */
  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val route = super.addRoute(method, transformers, action)
    route.copy(metadata = route.metadata.updated(Constants.RouteMetadataHttpMethodCacheKey, method))
  }

  def mount(ctx: ServletContext): Unit = {
    routes.entryPoints
      .flatMap(_.split("\\s+").tail.headOption.map(_.split("[:\\?]+").head))
      .distinct
      .flatMap { path => if (path.endsWith(".")) respondTo.map(format => path + format.name) else Seq(path) }
      .foreach { path => ctx.mount(this, path) }
  }

}
