package skinny.test

import javax.servlet.http._
import javax.servlet.ServletContext
import org.json4s._
import org.mockito.Mockito._
import skinny.engine.{ UnstableAccessValidation, EngineParams }
import skinny.engine.context.SkinnyEngineContext
import skinny.util.JSONStringOps
import scala.collection.concurrent.TrieMap
import skinny.controller.SkinnyControllerBase
import skinny.controller.feature.{ JSONParamsAutoBinderFeature, RequestScopeFeature }
import javax.servlet.http.HttpServletResponse

/**
 * Mock Controller Base.
 */
trait MockControllerBase extends SkinnyControllerBase with JSONParamsAutoBinderFeature {

  case class RenderCall(path: String)

  private val _requestScope = TrieMap[String, Any]()

  override def contextPath = ""

  override def initParameter(name: String): Option[String] = None

  private[this] lazy val mockRequest = {
    val req = new MockHttpServletRequest
    req.setAttribute(RequestScopeFeature.REQUEST_SCOPE_KEY, _requestScope)
    req
  }

  private[this] lazy val mockResponse = {
    new MockHttpServletResponse
  }

  override def request(implicit ctx: SkinnyEngineContext): HttpServletRequest = mockRequest

  override def response(implicit ctx: SkinnyEngineContext): HttpServletResponse = mockResponse

  override implicit def servletContext: ServletContext = mock(classOf[ServletContext])

  override implicit def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    SkinnyEngineContext.build(ctx, mockRequest, mockResponse, UnstableAccessValidation(true))
  }

  override def halt[T: Manifest](
    status: Integer = null,
    body: T = (),
    headers: Map[String, String] = Map.empty,
    reason: String = null): Nothing = {

    throw new MockHaltException(
      status = Option(status).map(_.intValue()),
      reason = Option(reason),
      headers = headers,
      body = body)
  }

  def getOutputStreamContents: String = {
    response.getOutputStream.toString
  }

  def getOutputStreamContents(charset: String): String = {
    response
      .getOutputStream
      .asInstanceOf[MockServletOutputStream]
      .toString(charset)
  }

  private[this] val _params = TrieMap[String, Seq[String]]()
  private def _scalatraParams = new EngineParams(_params.toMap)
  override def params(implicit ctx: SkinnyEngineContext) = {
    val mergedParams = (super.params(ctx) ++ _scalatraParams).mapValues(v => Seq(v))
    new EngineParams(if (_parsedBody.isDefined) {
      getMergedMultiParams(mergedParams, parsedBody(ctx).extract[Map[String, String]].mapValues(v => Seq(v)))
    } else {
      mergedParams
    })
  }

  def prepareParams(params: (String, String)*) = {
    _params ++= params.map { case (k, v) => k -> Seq(v) }
  }

  private[this] var _parsedBody: Option[JValue] = None
  override def parsedBody(implicit ctx: SkinnyEngineContext): JValue = {
    _parsedBody.getOrElse(JNothing)
  }

  def prepareJSONBodyRequest(json: String) = {
    _parsedBody = JSONStringOps.fromJSONStringToJValue(json)
  }

  // initialize this controller

  initializeRequestScopeAttributes(skinnyEngineContext)

}
