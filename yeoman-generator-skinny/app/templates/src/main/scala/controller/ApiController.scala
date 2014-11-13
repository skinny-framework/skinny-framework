package controller

import skinny._
import skinny.filter._

/**
 * The base controller for API endpoints.
 */
trait ApiController extends SkinnyApiController {

  /*
   * Handles when unexpected exceptions are thrown from controllers.
   */
  addErrorFilter {
    case e: Throwable =>
      // For example, logs a exception and responds with status 500.
      logger.error(e.getMessage, e)
      haltWithBody(500)
  }

}

