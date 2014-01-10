package skinny.filter

import skinny.controller.SkinnyController
import org.scalatra.ErrorHandler

trait ErrorPageFilter { self: SkinnyController =>

  def addErrorHandler(handler: ErrorHandler) = error(handler)

  addErrorHandler {
    case e: Throwable =>
      logger.error(e.getMessage, e)
      status = 500
      render("/error/500")
  }

}
