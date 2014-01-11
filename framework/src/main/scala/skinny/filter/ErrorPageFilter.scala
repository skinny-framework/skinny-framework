package skinny.filter

/**
 * Shows error page when unexpected exceptions are thrown from controllers.
 */
trait ErrorPageFilter extends SkinnyFilter { self: SkinnyFilterActivation =>

  addErrorFilter {
    case e: Throwable =>
      logger.error(e.getMessage, e)
      status = 500
      render("/error/500")
  }

}
