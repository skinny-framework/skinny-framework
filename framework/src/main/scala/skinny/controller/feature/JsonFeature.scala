package skinny.controller.feature

import org.scalatra.json.JacksonJsonSupport
import org.json4s._
import skinny.Format

/**
 * JSON response support.
 */
trait JSONFeature extends JacksonJsonSupport {

  /**
   * JSON format support implicitly.
   */
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  /**
   * Converts a value to JSON value.
   *
   * @param v value
   * @return JValue
   */
  def toJSON(v: Any): JValue = Extraction.decompose(v)

  /**
   * Converts a value to JSON string.
   *
   * @param v value
   * @return json string
   */
  def toJSONString(v: Any): String = compact(toJSON(v))

  /**
   * Converts a value to prettified JSON string.
   *
   * @param v value
   * @return json string
   */
  def toPrettyJSONString(v: Any): String = pretty(toJSON(v))

  /**
   * Extracts a value from JSON string.
   *
   * @param json json string
   * @tparam A type
   * @return value
   */
  def fromJSON[A](json: String)(implicit mf: Manifest[A]): Option[A] = parseOpt(StringInput(json)).map[A](_.extract[A])

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
