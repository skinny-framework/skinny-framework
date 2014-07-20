package skinny.controller

import skinny._
import skinny.controller.feature._
import skinny.validator.implicits.ParametersGetAsImplicits
import skinny.controller.implicits.ParamsPermitImplicits
import skinny.routing.implicits.RoutesAsImplicits
import org.scalatra._
import java.util.Locale
import skinny.util.StringUtil
import org.json4s._
import scala.xml._
import org.json4s.JsonAST.JArray
import org.json4s.JsonAST.JDouble
import skinny.I18n
import org.json4s.JsonAST.JInt
import org.json4s.JDecimal

trait SkinnyControllerBase
    extends org.scalatra.SkinnyScalatraBase
    with EnvFeature
    with RichRouteFeature
    with UrlGeneratorSupport
    with ExplicitRedirectFeature
    with ActionDefinitionFeature
    with RequestScopeFeature
    with BeforeAfterActionFeature
    with FutureOpsFeature
    with LocaleFeature
    with ValidationFeature
    with JSONFeature
    with TimeLoggingFeature
    with ThreadLocalRequestFeature
    with SnakeCasedParamKeysFeature
    with RoutesAsImplicits
    with ParametersGetAsImplicits
    with ParamsPermitImplicits
    with Logging {

  /**
   * Default charset.
   */
  lazy val charset: Option[String] = Some("utf-8")

  /**
   * Defines formats to be respond. By default, HTML, JSON, XML are available.
   *
   * @return formats
   */
  protected def respondTo: Seq[Format] = Seq(Format.HTML, Format.JSON, Format.XML)

  /**
   * Set Content-Type for the format if absent.
   *
   * @param format format
   */
  protected def setContentTypeIfAbsent()(implicit format: Format = Format.HTML): Unit = {
    // If Content-Type is already set, never overwrite it.
    if (contentType == null) {
      contentType = format.contentType + charset.map(c => s"; charset=${c}").getOrElse("")
    }
  }

  /**
   * Renders body which responds to the specified format (JSON, XML) if possible.
   *
   * @param entity entity
   * @param format format (HTML,JSON,XML...)
   * @return body if possible
   */
  protected def renderWithFormat(entity: Any)(implicit format: Format = Format.HTML): String = {
    setContentTypeIfAbsent()
    format match {
      case Format.XML =>
        val entityXml = convertJValueToXML(toJSON(entity)).toString
        s"""<?xml version="1.0" encoding="${charset.getOrElse("UTF-8")}"?><${xmlRootName}>${entityXml}</${xmlRootName}>"""
      case Format.JSON => toJSONString(entity)
      case _ => null
    }
  }

  /**
   * Halts with body which responds to the specified format.
   * @param httpStatus  http status
   * @param format format (HTML,JSON,XML...)
   * @tparam A response type
   * @return body if possible
   */
  protected def haltWithBody[A](httpStatus: Int)(implicit format: Format = Format.HTML): A = halt(status)

  /**
   * Provides code block with format. If absent, halt as status 406.
   *
   * @param format format
   * @param action action
   * @tparam A response type
   * @return result
   */
  protected def withFormat[A](format: Format)(action: => A): A = {
    respondTo.find(_ == format) getOrElse haltWithBody(406)
    setContentTypeIfAbsent()(format)
    action
  }

  /**
   * Creates skinny.I18n instance for current locale.
   *
   * @param locale current locale
   * @return i18n provider
   */
  protected def createI18n()(implicit locale: java.util.Locale = currentLocale.orNull[Locale]) = I18n(locale)

  /**
   * Converts string value to snake_case'd value.
   *
   * @param s string value
   * @return snake_case'd value
   */
  protected def toSnakeCase(s: String): String = StringUtil.toSnakeCase(s)

  protected def xmlRootName: String = "response"

  protected def xmlItemName: String = "item"

  /**
   * {@link org.json4s.Xml.toXml(JValue)}
   */
  private[this] def convertJValueToXML(json: JValue): NodeSeq = {
    def _toXml(name: String, json: JValue): NodeSeq = json match {
      case JObject(fields) => new XmlNode(name, fields flatMap { case (n, v) => _toXml(n, v) })
      case JArray(xs) => xs flatMap { v => _toXml(name, v) }
      case JInt(x) => new XmlElem(name, x.toString)
      case JDouble(x) => new XmlElem(name, x.toString)
      case JDecimal(x) => new XmlElem(name, x.toString)
      case JString(x) => new XmlElem(name, x)
      case JBool(x) => new XmlElem(name, x.toString)
      case JNull => new XmlElem(name, "null")
      case JNothing => Text("")
    }
    json match {
      case JObject(fields) => fields flatMap { case (n, v) => _toXml(n, v) }
      case x => _toXml(xmlItemName, x)
    }
  }
  private[this] class XmlNode(name: String, children: Seq[Node])
    extends Elem(null, name, xml.Null, TopScope, children.isEmpty, children: _*)
  private[this] class XmlElem(name: String, value: String)
    extends Elem(null, name, xml.Null, TopScope, Text(value).isEmpty, Text(value))

}
