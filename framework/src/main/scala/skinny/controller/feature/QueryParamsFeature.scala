package skinny.controller.feature

import javax.servlet.http.HttpServletRequest

import skinny.engine._

/**
 * Provides queryParams/queryMultiParams.
 */
trait QueryParamsFeature extends SkinnyEngineBase {

  /**
   * Returns query string multi parameters as a Map value.
   */
  def queryMultiParams(implicit request: HttpServletRequest): MultiParams = {
    new MultiParams(rl.MapQueryString.parseString(request.queryString))
  }

  /**
   * Returns query string parameters as a Map value.
   */
  def queryParams(implicit request: HttpServletRequest): Params = new EngineParams(queryMultiParams)

}
