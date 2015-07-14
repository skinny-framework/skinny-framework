package skinny.engine.base

import skinny.engine.SkinnyEngineBase
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.implicits.ServletApiImplicits
import skinny.engine.response.ResponseStatus

trait ResponseStatusAccessor extends ServletApiImplicits { self: SkinnyEngineBase =>

  /**
   * Gets the status code of the current response.
   */
  def status(implicit ctx: SkinnyEngineContext = context): Int = ctx.response.status.code

  /**
   * Sets the status code of the current response.
   */
  def status_=(code: Int)(implicit ctx: SkinnyEngineContext = context): Unit = {
    ctx.response.status = ResponseStatus(code)
  }

}
