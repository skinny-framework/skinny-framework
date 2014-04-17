package skinny.filter

import org.scalatra.ErrorHandler
import skinny.controller.SkinnyWebPageControllerFeatures

/**
 * Skinny Filter.
 *
 * For example:
 *
 * {{{
 *   class BooksController extends SkinnyController
 *     with TxPerRequestFiler
 *     with SkinnyFilterActivation {
 *
 *     // within a transaction
 *     def changeTitle = {
 *       if (...) {
 *         throw new UnexpectedErrorException
 *         // rollback
 *       } else {
 *         redirect(s"/books/\${id}")
 *         // commit
 *       }
 *     }
 *   }
 * }}}
 *
 * If you use Scatatra's filter (before/after not beforeAction/afterAction), be careful. It's pretty tricky.
 * Because Scalatra's filters would be applied for all the controllers difined below in ScalatraBootstrap.
 */
trait SkinnyRenderingFilter extends SkinnyFilter with SkinnyWebPageControllerFeatures {

  /**
   * Adds error handler which renders body to SkinnyController.
   *
   * @param handler
   */
  def addRenderingErrorFilter(handler: ErrorHandler) = {
    skinnyErrorFilters.update(WithRendering, handler)
  }

}
