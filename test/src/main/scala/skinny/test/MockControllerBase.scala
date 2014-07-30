package skinny.test

import javax.servlet.http._
import javax.servlet.ServletContext
import org.json4s._
import org.mockito.Mockito._
import org.scalatra._
import skinny.util.JSONStringOps
import scala.collection.concurrent.TrieMap
import skinny.controller.SkinnyControllerBase
import skinny.controller.feature.{ JSONParamsAutoBinderFeature, RequestScopeFeature }
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletOutputStream

/**
 * Mock Controller Base.
 */
trait MockControllerBase extends SkinnyControllerBase with JSONParamsAutoBinderFeature {

  case class RenderCall(path: String)

  private val _requestScope = TrieMap[String, Any]()

  override def servletContext: ServletContext = mock(classOf[ServletContext])
  override def contextPath = ""
  override def initParameter(name: String): Option[String] = None

  override implicit val request: HttpServletRequest = {
    val req = new MockHttpServletRequest
    req.setAttribute(RequestScopeFeature.REQUEST_SCOPE_KEY, _requestScope)
    req
  }

  override implicit val response: HttpServletResponse = {
    val res = new MockHttpServletResponse {
      val stubOutputStream = new MockServletOutputStream
      override def getOutputStream: ServletOutputStream = stubOutputStream
    }
    res
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

  def outputStreamContents: String = {
    response
      .asInstanceOf[MockHttpServletResponse]
      .getOutputStream
      .toString
  }

  private[this] val _params = TrieMap[String, Seq[String]]()
  private def _scalatraParams = new ScalatraParams(_params.toMap)
  override def params(implicit request: HttpServletRequest) = {
    val mergedParams = (super.params ++ _scalatraParams).mapValues(v => Seq(v))
    new ScalatraParams(if (_parsedBody.isDefined) {
      getMergedMultiParams(mergedParams, parsedBody.extract[Map[String, String]].mapValues(v => Seq(v)))
    } else {
      mergedParams
    })
  }

  def prepareParams(params: (String, String)*) = {
    _params ++= params.map { case (k, v) => k -> Seq(v) }
  }

  private[this] var _parsedBody: Option[JValue] = None
  override def parsedBody(implicit request: HttpServletRequest): JValue = {
    _parsedBody.getOrElse(JNothing)
  }

  def prepareJSONBodyRequest(json: String) = {
    _parsedBody = JSONStringOps.fromJSONStringToJValue(json)
  }

  // initialize this controller

  initializeRequestScopeAttributes

}
