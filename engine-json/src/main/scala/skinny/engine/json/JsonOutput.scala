package skinny.engine.json

import java.io.Writer

import org.json4s.Xml._
import org.json4s._
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.{ RenderPipeline, ApiFormats }

import scala.io.Codec
import scala.xml.XML

object JsonOutput {
  val VulnerabilityPrelude = ")]}',\n"
  val RosettaPrelude = "/**/"
}

trait JsonOutput[T] extends ApiFormats with JsonMethods[T] {

  import JsonOutput._
  /**
   * If a request is made with a parameter in jsonpCallbackParameterNames it will
   * be assumed that it is a JSONP request and the json will be returned as the
   * argument to a function with the name specified in the corresponding parameter.
   *
   * By default no parameterNames will be checked
   */
  def jsonpCallbackParameterNames: Iterable[String] = Nil

  /**
   * Whether or not to apply the jsonVulnerabilityGuard when rendering json.
   * @see http://haacked.com/archive/2008/11/20/anatomy-of-a-subtle-json-vulnerability.aspx
   */
  protected def jsonVulnerabilityGuard = false

  /**
   * Whether or not to apply the rosetta flash guard when rendering jsonp callbacks.
   * @see http://miki.it/blog/2014/7/8/abusing-jsonp-with-rosetta-flash/
   */
  protected def rosettaFlashGuard = true

  protected lazy val xmlRootNode = <resp></resp>

  protected def transformResponseBody(body: JValue) = body

  override protected def renderPipeline(implicit ctx: SkinnyEngineContext) = ({

    case JsonResult(jv) => jv

    case jv: JValue if format(ctx) == "xml" =>
      (contentType = formats("xml"))(ctx)
      writeJsonAsXml(
        transformResponseBody(jv),
        ctx.response.writer,
        ctx.response.characterEncoding.getOrElse(Codec.UTF8.name))

    case jv: JValue =>
      // JSON is always UTF-8
      ctx.response.characterEncoding = Some(Codec.UTF8.name)
      val writer = ctx.response.writer

      val jsonpCallback = for {
        paramName <- jsonpCallbackParameterNames
        callback <- params(ctx).get(paramName)
      } yield callback

      jsonpCallback match {
        case some :: _ =>
          // JSONP is not JSON, but JavaScript.
          (contentType = formats("js"))(ctx)
          // Status must always be 200 on JSONP, since it's loaded in a <script> tag.
          (status = 200)(ctx)
          if (rosettaFlashGuard) writer.write("/**/")
          writer.write("%s(%s);".format(some, compact(render(transformResponseBody(jv)))))
        case _ =>
          (contentType = formats("json"))(ctx)
          if (jsonVulnerabilityGuard) writer.write(VulnerabilityPrelude)
          writeJson(transformResponseBody(jv), writer)
          ()
      }
  }: RenderPipeline) orElse super.renderPipeline(ctx)

  protected def writeJsonAsXml(json: JValue, writer: Writer, characterEncoding: String) {
    if (json != JNothing)
      XML.write(
        writer,
        xmlRootNode.copy(child = toXml(json)),
        characterEncoding,
        xmlDecl = true,
        doctype = null)
  }

  protected def writeJson(json: JValue, writer: Writer)

}