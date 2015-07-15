package skinny.controller.feature

import java.io.{ InputStreamReader, InputStream }

import org.json4s.Xml._
import org.json4s._
import org.slf4j.LoggerFactory
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.json.{JSONOperations, JsonSupport}
import skinny.engine.{ SkinnyEngineBase, EngineParams, Params, ApiFormats }
import skinny.engine.routing.MatchedRoute
import skinny.logging.LoggerProvider

/**
 * Merging JSON request body into Scalatra params.
 *
 * When you'd like to avoid merging JSON request body into params in some actions, please separate controllers.
 */
trait JSONParamsAutoBinderFeature
    extends SkinnyEngineBase
    with JSONOperations
    with ApiFormats
    with LoggerProvider {

  /**
   * Merge parsedBody (JValue) into params if possible.
   */
  override def params(implicit ctx: SkinnyEngineContext): Params = {
    if (request(ctx).get(JsonSupport.ParsedBodyKey).isDefined) {
      try {
        val jsonParams: Map[String, Seq[String]] = parsedBody(ctx).extract[Map[String, String]].mapValues(v => Seq(v))
        val mergedParams: Map[String, Seq[String]] = getMergedMultiParams(multiParams(ctx), jsonParams)
        new EngineParams(mergedParams)
      } catch {
        case e: Exception =>
          logger.debug(s"Failed to parse JSON body because ${e.getMessage}")
          super.params(ctx)
      }
    } else {
      super.params(ctx)
    }
  }

  protected def getMergedMultiParams(
    params1: Map[String, Seq[String]], params2: Map[String, Seq[String]]): Map[String, Seq[String]] = {
    (params1.toSeq ++ params2.toSeq).groupBy(_._1).mapValues(_.flatMap(_._2))
  }

  // --------------------------
  // Avoid extending JacksonJsonSupport due to render method conflict

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
        } else if (cacheRequestBodyAsString) {
          readJsonFromBody(ctx.request.body)
        } else {
          readJsonFromStreamWithCharset(
            ctx.request.inputStream,
            ctx.request.characterEncoding.getOrElse(defaultCharacterEncoding)
          )
        }
      }
      transformRequestBody(bd)
    } else if (format == "xml") {
      val bd = {
        if (ct == "application/x-www-form-urlencoded") {
          multiParams(ctx).keys.headOption
            .map(readXmlFromBody)
            .getOrElse(JNothing)
        } else if (cacheRequestBodyAsString) readXmlFromBody(request(ctx).body)
        else readXmlFromStream(request(ctx).inputStream)
      }
      transformRequestBody(bd)
    } else JNothing
  } catch {
    case t: Throwable ⇒ {
      logger.error(s"Parsing the request body failed, because:", t)
      JNothing
    }
  }

  protected def readJsonFromStreamWithCharset(stream: InputStream, charset: String): JValue = {
    val rdr = new InputStreamReader(stream, charset)
    if (rdr.ready()) defaultObjectMapper.readValue(rdr, classOf[JValue])
    else {
      rdr.close()
      JNothing
    }
  }

  protected def readJsonFromBody(bd: String): JValue = {
    if (Option(bd).exists(_.trim.length > 0)) defaultObjectMapper.readValue(bd, classOf[JValue])
    else JNothing
  }

  protected def readJsonFromStream(stream: InputStream): JValue = {
    readJsonFromStreamWithCharset(stream, defaultCharacterEncoding)
  }

  protected def readXmlFromBody(bd: String): JValue = {
    if (Option(bd).exists(_.trim.length > 0)) {
      val JObject(JField(_, jv) :: Nil) = toJson(scala.xml.XML.loadString(bd))
      jv
    } else JNothing
  }

  protected def readXmlFromStream(stream: InputStream): JValue = {
    val rdr = new InputStreamReader(stream)
    if (rdr.ready()) {
      val JObject(JField(_, jv) :: Nil) = toJson(scala.xml.XML.load(rdr))
      jv
    } else JNothing
  }

  protected def transformRequestBody(body: JValue) = body

  override protected def invoke(matchedRoute: MatchedRoute) = {
    withRouteMultiParams(Some(matchedRoute)) {
      val mt = request.contentType.fold("application/x-www-form-urlencoded")(_.split(";").head)
      val fmt = mimeTypes get mt getOrElse "html"
      if (shouldParseBody(fmt)) {
        request(ParsedBodyKey) = parseRequestBody(fmt).asInstanceOf[AnyRef]
      }
      super.invoke(matchedRoute)
    }
  }

  protected def shouldParseBody(fmt: String)(implicit ctx: SkinnyEngineContext) = {
    (fmt == "json" || fmt == "xml") && !ctx.request.requestMethod.isSafe && parsedBody(ctx) == JNothing
  }

  def parsedBody(implicit ctx: SkinnyEngineContext): JValue = {
    ctx.request.get(ParsedBodyKey).fold({
      val fmt = requestFormat(ctx)
      var bd: JValue = JNothing
      if (fmt == "json" || fmt == "xml") {
        bd = parseRequestBody(fmt)(ctx)
        ctx.request(ParsedBodyKey) = bd.asInstanceOf[AnyRef]
      }
      bd
    })(_.asInstanceOf[JValue])
  }

  // --------------------------

}
