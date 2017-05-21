package skinny.filter

import skinny.controller.feature.SkinnyControllerCommonBase

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
  * If you use Skinny Micro's filter - before/after, be careful. It's pretty tricky.
  * Because Skinny Micro's filters would be applied for all the controllers defined below in Bootstrap.
  * Just using beforeAction/afterAction is highly recommended.
  */
trait SkinnyFilter extends SkinnyControllerCommonBase with SkinnyFilterActivation
