package skinny.controller.feature

import org.scalatra._
import org.scalatra.scalate._
import org.json4s._
import org.scalatra.json._
import skinny.Format
import scala.xml._
import grizzled.slf4j.Logging

trait TemplateEngineFeature
    extends ScalatraBase
    with RequestScopeFeature
    with ScalateSupport
    with JacksonJsonSupport
    with Logging {

  protected implicit val jsonFormats: Formats = DefaultFormats

  lazy val format: Format = Format.HTML
  lazy val charset: Option[String] = Some("utf-8")

  def render(path: String)(implicit format: Format = Format.HTML): String = {
    if (contentType == null) {
      contentType = format.contentType + charset.map(c => s"; charset=${c}").getOrElse("")
    }

    if (templateExists(path)) {
      renderWithTemplate(path)
    } else if (format == Format.HTML) {
      throw new IllegalStateException(s"View template not found. (expected: ${templatePath(path)})")
    } else {
      logger.debug(s"Template for ${path} not found.")
      val entity = (for {
        resourcesName <- requestScope[String]("resourcesName")
        resources <- requestScope[Any](resourcesName)
      } yield resources) getOrElse {
        for {
          resourceName <- requestScope[String]("resourceName")
          resource <- requestScope[Any](resourceName)
        } yield resource
      }
      renderWithFormat(entity) getOrElse haltWithBody(404)
    }
  }

  protected def templatePath(path: String)(implicit format: Format = Format.HTML): String

  protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean

  protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String

  protected def renderWithFormat(entity: Any)(implicit format: Format = Format.HTML): Option[String] = {
    Option {
      format match {
        case Format.XML =>
          val entityXml = toXml(Extraction.decompose(entity)).toString
          try {
            scala.xml.XML.loadString(entityXml)
            s"""<?xml version="1.0" encoding="${charset.getOrElse("UTF-8")}"?>${entityXml}"""
          } catch {
            case e: Exception =>
              s"""<?xml version="1.0" encoding="${charset.getOrElse("UTF-8")}"?><response>${entityXml}</response>"""
          }
        case Format.JSON =>
          val jsonString = compact(render(Extraction.decompose(entity)))
          compact(render(parse(jsonString).underscoreKeys))
        case _ => null
      }
    }
  }

  def haltWithBody[A](httpStatus: Int)(implicit format: Format = Format.HTML): A = {
    val bodyOpt = format match {
      case Format.HTML => Option(render(s"/error/${httpStatus}"))
      case _ => renderWithFormat(Map("status" -> httpStatus, "message" -> ResponseStatus(httpStatus).message))
    }
    bodyOpt.map { body =>
      halt(status = httpStatus, body = body)
    } getOrElse {
      halt(status = httpStatus)
    }
  }

  /**
   * {@link org.json4s.Xml.toXml(JValue)}
   */
  private def toXml(json: JValue): NodeSeq = {
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
      case x => _toXml("item", x)
    }
  }
  private class XmlNode(name: String, children: Seq[Node])
    extends Elem(null, name, xml.Null, TopScope, children.isEmpty, children: _*)
  private class XmlElem(name: String, value: String)
    extends Elem(null, name, xml.Null, TopScope, Text(value).isEmpty, Text(value))

}
