package skinny.controller.feature

import skinny.micro._
import skinny.micro.context.SkinnyContext

/**
 * Provides queryParams/queryMultiParams.
 */
trait QueryParamsFeature extends SkinnyMicroBase {

  /**
   * Returns query string multi parameters as a Map value.
   */
  def queryMultiParams(implicit ctx: SkinnyContext): MultiParams = {
    new MultiParams(rl.MapQueryString.parseString(ctx.request.queryString))
  }

  /**
   * Returns query string parameters as a Map value.
   */
  def queryParams(implicit ctx: SkinnyContext): Params = new SkinnyMicroParams(queryMultiParams(ctx))

}
