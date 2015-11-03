package skinny.filter

/**
 * Shows error page when unexpected exceptions are thrown from controllers.
 */
trait AsyncErrorPageFilter extends AsyncSkinnyRenderingFilter {

  addRenderingErrorFilter {
    case scala.util.control.NonFatal(e) =>
      logger.error(e.getMessage, e)
      try {
        status = 500
        implicit val ctx = context
        render("/error/500")
      } catch {
        case e: Exception => throw e
      }
  }

}
