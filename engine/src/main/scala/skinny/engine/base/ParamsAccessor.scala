package skinny.engine.base

import skinny.engine._
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.implicits.ServletApiImplicits

/**
 * Provides params/multiParams DSL.
 */
trait ParamsAccessor extends ServletApiImplicits {

  /**
   * The current multi params.
   *
   * Multi params are a result of merging the standard request params (query string or post params) with the route
   * parameters extracted from the route matchers of the current route.
   * The default value for an unknown param is the empty sequence.
   * Invalid outside `handle`.
   */
  def multiParams(implicit ctx: SkinnyEngineContext): MultiParams = {
    val req = ctx.request
    val read = req.contains("MultiParamsRead")
    val found = req.get(MultiParamsKey) map (
      _.asInstanceOf[MultiParams] ++ (if (read) Map.empty else req.multiParameters)
    )
    val multi = found.getOrElse(req.multiParameters)
    req("MultiParamsRead") = new {}
    req(MultiParamsKey) = multi
    multi.withDefaultValue(Seq.empty)
  }

  def multiParams(key: String)(implicit ctx: SkinnyEngineContext): Seq[String] = multiParams(ctx)(key)

  def params(key: String)(implicit ctx: SkinnyEngineContext): String = params(ctx)(key)

  def params(key: Symbol)(implicit ctx: SkinnyEngineContext): String = params(ctx)(key)

  def params(implicit ctx: SkinnyEngineContext): Params = new EngineParams(multiParams(ctx))

}
