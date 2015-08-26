package skinny.controller.feature

import skinny.micro._
import skinny.micro.context.SkinnyContext

/**
 * Provides formParams/formMultiParams.
 */
trait FormParamsFeature extends SkinnyMicroBase with QueryParamsFeature {

  /**
   * Returns query string multi parameters as a Map value.
   */
  def formMultiParams(implicit ctx: SkinnyContext): MultiParams = {
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
  def formParams(implicit ctx: SkinnyContext): Params = new SkinnyMicroParams(formMultiParams(ctx))

}
