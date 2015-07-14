package skinny.controller.feature

import javax.servlet.http.HttpServletRequest

import skinny.engine._

/**
 * Provides formParams/formMultiParams.
 */
trait FormParamsFeature extends SkinnyEngineBase with QueryParamsFeature {

  /**
   * Returns query string multi parameters as a Map value.
   */
  def formMultiParams(implicit request: HttpServletRequest): MultiParams = {
    multiParams.map {
      case (k, vs) =>
        queryMultiParams.find(_._1 == k) match {
          case Some((k, queryValues)) => k -> vs.diff(queryValues)
          case _ => k -> vs
        }
    }
  }

  /**
   * Returns query string parameters as a Map value.
   */
  def formParams(implicit request: HttpServletRequest): Params = new EngineParams(formMultiParams)

}
