package skinny.util

import org.json4s._

object JSONStringOps extends JSONStringOps

/**
 * Easy-to-use JSON String Operation.
 */
trait JSONStringOps extends jackson.JsonMethods {

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
  def toJSONString(v: Any, underscoreKeys: Boolean = true): String = {
    if (underscoreKeys) compact(parse(compact(toJSON(v))).underscoreKeys)
    else compact(toJSON(v))
  }

  /**
   * Converts a value to prettified JSON string.
   *
   * @param v value
   * @return json string
   */
  def toPrettyJSONString(v: Any, underscoreKeys: Boolean = true): String = {
    if (underscoreKeys) pretty(parse(compact(toJSON(v))).underscoreKeys)
    else pretty(toJSON(v))
  }

  /**
   * Extracts a value from JSON string.
   *
   * @param json json string
   * @tparam A type
   * @return value
   */
  def fromJSONString[A](json: String)(implicit mf: Manifest[A]): Option[A] = {
    parseOpt(StringInput(json)).map(v => v.camelizeKeys).map[A](_.extract[A])
  }

}