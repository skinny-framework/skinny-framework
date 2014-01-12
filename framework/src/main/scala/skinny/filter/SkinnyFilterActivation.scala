package skinny.filter

import skinny.controller._
import org.scalatra._

/**
 * Activates skinny filters.
 */
trait SkinnyFilterActivation { self: SkinnyControllerBase =>

  sealed trait RenderingRequired
  case object WithRendering extends RenderingRequired
  case object WithoutRendering extends RenderingRequired

  /**
   * Registered error handlers.
   */
  protected lazy val skinnyErrorFilters = new scala.collection.concurrent.TrieMap[RenderingRequired, ErrorHandler]

  /**
   * Adds error handler which doesn't return result to SkinnyController.
   *
   * @param handler
   */
  def addErrorFilter(handler: ErrorHandler) = {
    skinnyErrorFilters.update(WithoutRendering, handler)
  }

  /**
   * Start applying all the filters
   */
  beforeAction() {
    // initialize error handler
    val filtersTraverse: PartialFunction[Throwable, Any] = {
      case (t: Throwable) =>
        skinnyErrorFilters.foldLeft(null.asInstanceOf[Any]) {
          case (last, (WithoutRendering, filter)) =>
            try filter.apply(t)
            catch {
              case e: Exception => logger.error(s"Failed to apply SkinnyFilter (error: ${e.getMessage})", e)
            }
            last
          case (last, (WithRendering, filter)) =>
            try filter.apply(t)
            catch {
              case e: Exception =>
                logger.error(s"Failed to apply SkinnyFilter (error: ${e.getMessage})", e)
                throw e
            }
        }
    }
    error(filtersTraverse)
  }

}
