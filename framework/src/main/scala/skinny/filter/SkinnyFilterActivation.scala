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
  protected lazy val skinnyErrorFilters = new scala.collection.concurrent.TrieMap[RenderingRequired, Seq[ErrorHandler]]

  /**
   * Adds error handler which doesn't return result to SkinnyController.
   *
   * @param handler
   */
  def addErrorFilter(handler: ErrorHandler) = {
    val mergedHandlers = skinnyErrorFilters.get(WithoutRendering).map(hs => hs :+ handler).getOrElse(Seq(handler))
    skinnyErrorFilters.update(WithoutRendering, mergedHandlers)
  }

  /**
   * Start applying all the filters
   */
  beforeAction() {
    // combine error filters
    val filtersTraverse: PartialFunction[Throwable, Any] = {
      case (t: Throwable) =>

        skinnyErrorFilters.get(WithoutRendering).foreach { handlers =>
          handlers.foreach { handler =>
            // just apply this filter and return the existing body.
            try handler.apply(t)
            catch { case e: Exception => logger.error(s"Failed to apply SkinnyFilter (error: ${e.getMessage})", e) }
          }
        }

        var rendered = false

        // rendering body
        val body: Any = skinnyErrorFilters.get(WithRendering).map { handlers =>
          handlers.foldLeft(null.asInstanceOf[Any]) {
            case (body, handler) =>
              // just apply this filter and return the existing body.
              if (rendered) {
                logger.error("This response is marked as rendered because other RenderingFilter already did the stuff.")
                body
              } else {
                rendered = true
                try handler.apply(t)
                catch {
                  case e: Exception =>
                    logger.error(s"Failed to apply SkinnyFilter (error: ${e.getMessage})", e)
                    body
                }
              }
          }
        }.getOrElse(null)

        if (rendered) body else throw t
    }
    // register combined error filters
    error(filtersTraverse)
  }

}
