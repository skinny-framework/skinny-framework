package skinny.controller.feature

import org.scalatra.json.JacksonJsonSupport
import skinny.{ Format }
import skinny.util.JSONStringOps

/**
 * JSON response support.
 */
trait JSONFeature extends JacksonJsonSupport with JSONStringOps {

  /**
   * Returns JSON response.
   *
   * @param entity entity
   * @param charset charset
   * @param prettify prettify if true
   * @return body
   */
  def responseAsJSON(entity: Any, charset: Option[String] = Some("utf-8"), prettify: Boolean = false): String = {
    // If Content-Type is already set, never overwrite it.
    if (contentType == null) {
      contentType = Format.JSON.contentType + charset.map(c => s"; charset=${c}").getOrElse("")
    }
    if (prettify) toPrettyJSONString(entity) else toJSONString(entity)
  }

}
