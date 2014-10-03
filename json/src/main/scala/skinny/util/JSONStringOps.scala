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
   * @param underscoreKeys apply #underscoreKeys keys if true
   * @return json string
   */
  def toJSONString(v: Any, underscoreKeys: Boolean = useUnderscoreKeysForJSON): String = {
    if (underscoreKeys) compact(parse(compact(toJSON(v))).underscoreKeys)
    else compact(toJSON(v))
  }

  /**
   * Converts a value to JSON string without key conversions.
   *
   * @param v value
   * @return json string
   */
  def toJSONStringAsIs(v: Any): String = toJSONString(v, false)

  /**
   * Converts a value to prettified JSON string.
   *
   * @param v value
   * @param underscoreKeys apply #underscoreKeys keys if true
   * @return json string
   */
  def toPrettyJSONString(v: Any, underscoreKeys: Boolean = useUnderscoreKeysForJSON): String = {
    if (underscoreKeys) pretty(parse(compact(toJSON(v))).underscoreKeys)
    else pretty(toJSON(v))
  }

  /**
   * Converts a value to prettified JSON string without key conversions.
   *
   * @param v value
   * @return json string
   */
  def toPrettyJSONStringAsIs(v: Any): String = toPrettyJSONString(v, false)

  /**
   * Extracts a value from JSON string.
   * NOTE: When you convert to Map objects, be aware that underscoreKeys is false by default.
   *
   * @param json json string
   * @param underscoreKeys apply #underscoreKeys keys if true
   * @param asIs never apply key conversions if true
   * @tparam A return type
   * @return value
   */
  def fromJSONString[A](json: String, underscoreKeys: Boolean = false, asIs: Boolean = false)(implicit mf: Manifest[A]): Option[A] = {
    fromJSONStringToJValue(json, underscoreKeys, asIs).map[A](_.extract[A])
  }

  /**
   * Extracts a value from JSON string. The keys will be used as-is.
   *
   * {{{
   *   case class Something(fooBar_baz: String)
   *   val something = new Something("foo")
   *   val json = toJSONString(something)
   *   val something2 = fromJSONStringAsIs[Something](json)
   *   something2.map(_.fooBar_baz) should equal(Some(something.fooBar_Baz))
   * }}}
   *
   * @param json json string
   * @param mf manifest
   * @tparam A return type
   * @return value
   */
  def fromJSONStringAsIs[A](json: String)(implicit mf: Manifest[A]): Option[A] = fromJSONString(json, false, true)

  /**
   * Extracts a JSON value from JSON string.
   * NOTE: When you convert to Map objects, be aware that underscoreKeys is false by default.
   *
   * @param json json string
   * @param underscoreKeys underscore keys
   * @return value
   */
  def fromJSONStringToJValue(json: String, underscoreKeys: Boolean = false, asIs: Boolean = false): Option[JValue] = {
    val pureJson = if (useJSONVulnerabilityProtection &&
      json.startsWith(prefixForJSONVulnerabilityProtection)) {
      json.replace(prefixForJSONVulnerabilityProtection, "")
    } else {
      json
    }
    parseOpt(StringInput(pureJson)).map { v =>
      if (asIs) v
      else if (underscoreKeys) v.underscoreKeys
      else v.camelizeKeys
    }
  }

}
