package skinny.controller.feature

import skinny.controller.Constants
import org.scalatra._
import skinny._
import javax.servlet.{ Filter, DispatcherType }

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

  private def toNormalizedRoutePath(path: String): String = path match {
    case "/" => "/"
    case p if p.endsWith("/*") => p
    case p if p.endsWith("/") => p + "*"
    case _ => path + "/*"
  }

  private def allRoutePaths: Seq[String] = {
    routes.entryPoints
      .flatMap(_.split("\\s+").tail.headOption.map(_.split("[:\\?]+").head))
      .distinct
      .flatMap { path =>
        if (path.endsWith(".")) respondTo.map(format => path + format.name)
        else Seq(path)
      }
  }

  def mount(ctx: ServletContext): Unit = {
    this match {
      case filter: Filter =>
        allRoutePaths.foreach { path =>
          val name = this.getClass.getName
          val registration = {
            Option(ctx.getFilterRegistration(name)).getOrElse(ctx.addFilter(name, this.asInstanceOf[Filter]))
          }
          registration.addMappingForUrlPatterns(
            java.util.EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, toNormalizedRoutePath(path))
        }
      case _ => ctx.mount(this, "/")
    }
  }

}
