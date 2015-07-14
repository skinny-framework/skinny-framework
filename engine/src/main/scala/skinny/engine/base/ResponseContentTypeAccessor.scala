package skinny.engine.base

import skinny.engine.SkinnyEngineBase
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.implicits.ServletApiImplicits

trait ResponseContentTypeAccessor extends ServletApiImplicits { self: SkinnyEngineBase =>

  /**
   * Gets the content type of the current response.
   */
  def contentType(implicit ctx: SkinnyEngineContext = context): String = ctx.response.contentType.orNull[String]

  /**
   * Sets the content type of the current response.
   */
  def contentType_=(contentType: String)(implicit ctx: SkinnyEngineContext = context): Unit = {
    ctx.response.contentType = Option(contentType)
  }

}
