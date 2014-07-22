package skinny.util

import org.json4s._

object JSONStringOps extends JSONStringOps

/**
 * Easy-to-use JSON String Operation.
 */
trait JSONStringOps extends jackson.JsonMethods {

  /**
   * Use the prefix for JSON Vulnerability Protection.
   * see: "https://docs.angularjs.org/api/ng/service/$http"
   */
  protected def useJSONVulnerabilityProtection: Boolean = false

  /**
   * the prefix for JSON Vulnerability Protection.
   * see: "https://docs.angularjs.org/api/ng/service/$http"
   */
  protected def prefixForJSONVulnerabilityProtection: String = ")]}',\n"

  /**
   * Default key policy.
   */
  protected def useUnderscoreKeysForJSON: Boolean = true

  /**
   * JSON format support implicitly.
   */
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  /**
   * Returns JSON string value.
   *
   * @param value value
   */
  override def compact(value: JValue): String = {
    val json = super.compact(value)
    if (useJSONVulnerabilityProtection) prefixForJSONVulnerabilityProtection + json
    else json
  }

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
  def toJSONString(v: Any, underscoreKeys: Boolean = useUnderscoreKeysForJSON): String = {
    if (underscoreKeys) compact(parse(compact(toJSON(v))).underscoreKeys)
    else compact(toJSON(v))
  }

  /**
   * Converts a value to prettified JSON string.
   *
   * @param v value
   * @return json string
   */
  def toPrettyJSONString(v: Any, underscoreKeys: Boolean = useUnderscoreKeysForJSON): String = {
    if (underscoreKeys) pretty(parse(compact(toJSON(v))).underscoreKeys)
    else pretty(toJSON(v))
  }

  /**
   * Extracts a value from JSON string.
   * NOTE: When you convert to Map objects, be aware that underscoreKeys is false by default.
   *
   * @param json json string
   * @tparam A type
   * @return value
   */
  def fromJSONString[A](json: String, underscoreKeys: Boolean = false)(implicit mf: Manifest[A]): Option[A] = {
    fromJSONStringToJValue(json, underscoreKeys).map[A](_.extract[A])
  }

  def fromJSONStringToJValue(json: String, underscoreKeys: Boolean = false): Option[JValue] = {
    parseOpt(StringInput(json)).map { v =>
      if (underscoreKeys) v.underscoreKeys else v.camelizeKeys
    }
  }

}
