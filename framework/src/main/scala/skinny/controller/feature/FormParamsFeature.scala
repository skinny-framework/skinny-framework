package skinny.controller.feature

import skinny.engine._
import skinny.engine.context.SkinnyEngineContext

/**
 * Provides formParams/formMultiParams.
 */
trait FormParamsFeature extends SkinnyEngineBase with QueryParamsFeature {

  /**
   * Returns query string multi parameters as a Map value.
   */
  def formMultiParams(implicit ctx: SkinnyEngineContext): MultiParams = {
    multiParams(ctx).map {
      case (k, vs) =>
        queryMultiParams(ctx).find(_._1 == k) match {
          case Some((k, queryValues)) => k -> vs.diff(queryValues)
          case _ => k -> vs
        }
    }
  }

  /**
   * Returns query string parameters as a Map value.
   */
  def formParams(implicit ctx: SkinnyEngineContext): Params = new EngineParams(formMultiParams(ctx))

}
