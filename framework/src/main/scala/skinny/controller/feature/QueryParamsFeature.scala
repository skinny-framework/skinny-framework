package skinny.controller.feature

import skinny.engine._
import skinny.engine.context.SkinnyEngineContext

/**
 * Provides queryParams/queryMultiParams.
 */
trait QueryParamsFeature extends SkinnyEngineBase {

  /**
   * Returns query string multi parameters as a Map value.
   */
  def queryMultiParams(implicit ctx: SkinnyEngineContext): MultiParams = {
    new MultiParams(rl.MapQueryString.parseString(ctx.request.queryString))
  }

  /**
   * Returns query string parameters as a Map value.
   */
  def queryParams(implicit ctx: SkinnyEngineContext): Params = new EngineParams(queryMultiParams(ctx))

}
