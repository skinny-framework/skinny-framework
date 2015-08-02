package skinny.engine.base

import skinny.engine.{ SkinnyEngineBase, ApiFormats }
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.implicits.ServletApiImplicits

/**
 * Provides accessor for request format.
 */
trait RequestFormatAccessor extends ServletApiImplicits { self: SkinnyEngineBase =>

  /**
   * Explicitly sets the request-scoped format.  This takes precedence over
   * whatever was inferred from the request.
   */
  def format_=(formatValue: String)(implicit ctx: SkinnyEngineContext = context): Unit = {
    ctx.request(ApiFormats.FormatKey) = formatValue
  }

}