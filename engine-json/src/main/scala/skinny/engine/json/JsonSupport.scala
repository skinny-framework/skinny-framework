package skinny.engine.json

import java.io.{ InputStream, InputStreamReader }

import org.json4s.Xml._
import org.json4s._
import skinny.engine.context.SkinnyEngineContext
import org.slf4j.LoggerFactory

import javax.xml.parsers.SAXParserFactory
import skinny.engine.routing.MatchedRoute

import scala.xml.{ Elem, XML }
import scala.xml.factory.XMLLoader

object JsonSupport {

  val ParsedBodyKey = "skinny.engine.json.ParsedBody"
}

trait JsonSupport[T] extends JsonOutput[T] {

  import JsonSupport._

  private[this] val logger = LoggerFactory.getLogger(getClass)

  private[this] val _defaultCacheRequestBody = true

  protected def cacheRequestBodyAsString: Boolean = _defaultCacheRequestBody

  protected def parseRequestBody(format: String)(implicit ctx: SkinnyEngineContext) = try {
    val ct = ctx.request.contentType getOrElse ""
    if (format == "json") {
      val bd = {
        if (ct == "application/x-www-form-urlencoded") {
          multiParams(ctx).keys.headOption
            .map(readJsonFromBody)
            .getOrElse(JNothing)
        } else if (cacheRequestBodyAsString) readJsonFromBody(ctx.request.body)
        else readJsonFromStreamWithCharset(ctx.request.inputStream, ctx.request.characterEncoding getOrElse defaultCharacterEncoding)
      }
      transformRequestBody(bd)
    } else if (format == "xml") {
      val bd = {
        if (ct == "application/x-www-form-urlencoded") {
          multiParams(ctx).keys.headOption
            .map(readXmlFromBody)
            .getOrElse(JNothing)
        } else if (cacheRequestBodyAsString) readXmlFromBody(ctx.request.body)
        else readXmlFromStream(ctx.request.inputStream)
      }
      transformRequestBody(bd)
    } else JNothing
  } catch {
    case t: Throwable ⇒ {
      logger.error(s"Parsing the request body failed, because:", t)
      JNothing
    }
  }

  protected def readJsonFromBody(bd: String): JValue

  protected def readJsonFromStreamWithCharset(stream: InputStream, charset: String): JValue

  protected def readJsonFromStream(stream: InputStream): JValue = readJsonFromStreamWithCharset(stream, defaultCharacterEncoding)

  def secureXML: XMLLoader[Elem] = {
    val parserFactory = SAXParserFactory.newInstance()
    parserFactory.setNamespaceAware(false)
    parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    val saxParser = parserFactory.newSAXParser()
    XML.withSAXParser(saxParser)
  }

  protected def readXmlFromBody(bd: String): JValue = {
    if (bd.nonBlank) {
      val JObject(JField(_, jv) :: Nil) = toJson(secureXML.loadString(bd))
      jv
    } else JNothing
  }
  protected def readXmlFromStream(stream: InputStream): JValue = {
    val rdr = new InputStreamReader(stream)
    if (rdr.ready()) {
      val JObject(JField(_, jv) :: Nil) = toJson(secureXML.load(rdr))
      jv
    } else JNothing
  }
  protected def transformRequestBody(body: JValue) = body

  override protected def invoke(matchedRoute: MatchedRoute) = {
    withRouteMultiParams(Some(matchedRoute)) {
      val mt = request.contentType.fold("application/x-www-form-urlencoded")(_.split(";").head)
      val fmt = mimeTypes get mt getOrElse "html"
      if (shouldParseBody(fmt)(context)) {
        request(context)(ParsedBodyKey) = parseRequestBody(fmt)(context).asInstanceOf[AnyRef]
      }
      super.invoke(matchedRoute)
    }
  }

  protected def shouldParseBody(fmt: String)(
    implicit ctx: SkinnyEngineContext) = {
    (fmt == "json" || fmt == "xml") &&
      !ctx.request.requestMethod.isSafe &&
      parsedBody(ctx) == JNothing
  }

  def parsedBody(implicit ctx: SkinnyEngineContext): JValue = ctx.request.get(ParsedBodyKey).fold({
    val fmt = requestFormat(ctx)
    var bd: JValue = JNothing
    if (fmt == "json" || fmt == "xml") {
      bd = parseRequestBody(fmt)(ctx)
      ctx.request(ParsedBodyKey) = bd.asInstanceOf[AnyRef]
    }
    bd
  })(_.asInstanceOf[JValue])

}