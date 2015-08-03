package skinny.json

import com.fasterxml.jackson.databind.{ DeserializationFeature, ObjectMapper, ObjectWriter }
import org.json4s._
import org.json4s.jackson.Json4sScalaModule

import scala.util.control.Exception._

/**
 * Easy-to-use JSON String Operation.
 */
trait JSONStringOps extends { config: JSONStringOpsConfig =>

  // -------------------------------
  // Avoid extending org.json4s.jackson.JsonMethods due to #render method conflict
  // -------------------------------

  private[this] lazy val _defaultMapper = {
    val m = new ObjectMapper()
    m.registerModule(new Json4sScalaModule)
    m
  }
  private[this] def mapper = _defaultMapper

  def defaultObjectMapper: ObjectMapper = mapper

  private[this] def parse(in: JsonInput, useBigDecimalForDouble: Boolean = false): JValue = {
    mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, useBigDecimalForDouble)
    in match {
      case StringInput(s) => mapper.readValue(s, classOf[JValue])
      case ReaderInput(rdr) => mapper.readValue(rdr, classOf[JValue])
      case StreamInput(stream) => mapper.readValue(stream, classOf[JValue])
      case FileInput(file) => mapper.readValue(file, classOf[JValue])
    }
  }

  private[this] def parseOpt(in: JsonInput, useBigDecimalForDouble: Boolean = false): Option[JValue] = allCatch opt {
    parse(in, useBigDecimalForDouble)
  }

  private[this] def render(value: JValue): JValue = value

  private[this] def pretty(d: JValue): String = {
    val writer: ObjectWriter = mapper.writerWithDefaultPrettyPrinter()
    writer.writeValueAsString(d)
  }

  def asJValue[T](obj: T)(implicit writer: Writer[T]): JValue = writer.write(obj)

  def fromJValue[T](json: JValue)(implicit reader: Reader[T]): T = reader.read(json)

  /**
   * Returns JSON string value.
   *
   * @param value value
   */
  def compact(value: JValue): String = {
    val json = mapper.writeValueAsString(value)
    if (config.useJSONVulnerabilityProtection) prefixForJSONVulnerabilityProtection + json
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
  def toJSONString(v: Any, underscoreKeys: Boolean = config.useUnderscoreKeysForJSON): String = {
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
  def toPrettyJSONString(v: Any, underscoreKeys: Boolean = config.useUnderscoreKeysForJSON): String = {
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
    val pureJson = if (config.useJSONVulnerabilityProtection &&
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

object JSONStringOps
  extends JSONStringOps
  with JSONStringOpsConfig
