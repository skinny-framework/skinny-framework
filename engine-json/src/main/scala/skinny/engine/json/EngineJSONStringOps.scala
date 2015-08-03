package skinny.engine.json

import skinny.engine.{ Format, SkinnyEngineBase }
import skinny.engine.context.SkinnyEngineContext
import skinny.json.{ JSONStringOpsConfig, JSONStringOps }

/**
 * JSON response support.
 */
trait EngineJSONStringOps extends JSONStringOps with JSONStringOpsConfig { self: SkinnyEngineBase =>

  /**
   * Returns JSON response.
   *
   * @param entity entity
   * @param charset charset
   * @param prettify prettify if true
   * @return body
   */
  def responseAsJSON(
    entity: Any,
    charset: Option[String] = Some("utf-8"),
    prettify: Boolean = false,
    underscoreKeys: Boolean = self.useUnderscoreKeysForJSON)(implicit ctx: SkinnyEngineContext): String = {

    // If Content-Type is already set, never overwrite it.
    if (contentType(ctx) == null) {
      (contentType = Format.JSON.contentType + charset.map(c => s"; charset=${c}").getOrElse(""))(ctx)
    }

    if (prettify) toPrettyJSONString(entity)
    else toJSONString(entity, underscoreKeys)
  }

}
