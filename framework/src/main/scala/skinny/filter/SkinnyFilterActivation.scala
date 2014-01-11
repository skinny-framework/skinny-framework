package skinny.filter

import skinny.controller._
import org.scalatra._

/**
 * Activates skinny filters.
 */
trait SkinnyFilterActivation { self: SkinnyControllerBase =>

  /**
   * Registered error handlers.
   */
  private[this] lazy val skinnyErrorFilters = new scala.collection.mutable.ListBuffer[ErrorHandler]

  /**
   * Adds error handler to SkinnyController.
   *
   * @param handler
   */
  def addErrorFilter(handler: ErrorHandler) = {
    if (skinnyErrorFilters == null) {
      throw new IllegalStateException("SkinnyFilterActivation should be mixed in after filters?")
    } else {
      skinnyErrorFilters.append(handler)
    }
  }

  /**
   * Start applying all the filters
   */
  beforeAction() {
    // initialize error handler
    val filtersTraverse: PartialFunction[Throwable, Any] = {
      case (t: Throwable) =>
        skinnyErrorFilters.foldLeft(null.asInstanceOf[Any]) {
          case (last, filter: ErrorHandler) =>
            try filter.apply(t)
            catch {
              case e: Exception =>
                logger.error(s"Failed to apply SkinnyFilter (error: ${e.getMessage})", e)
                last
            }
        }
    }
    error(filtersTraverse)
  }

}
