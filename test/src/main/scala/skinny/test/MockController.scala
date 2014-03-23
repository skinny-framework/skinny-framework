package skinny.test

import javax.servlet.http._
import javax.servlet.ServletContext
import org.mockito.Mockito._
import org.scalatra._
import scala.collection.concurrent.TrieMap
import skinny.controller.feature.RequestScopeFeature
import skinny.Format
import skinny.controller.SkinnyControllerBase

trait MockController extends SkinnyControllerBase {

  case class RenderCall(path: String)

  private val _requestScope = TrieMap[String, Any]()

  override def servletContext: ServletContext = mock(classOf[ServletContext])
  override def contextPath = ""
  override def initParameter(name: String): Option[String] = None

  override val request: HttpServletRequest = {
    val req = new MockHttpServletRequest
    req.setAttribute(RequestScopeFeature.REQUEST_SCOPE_KEY, _requestScope)
    req
  }

  override val response: HttpServletResponse = {
    val res = new MockHttpServletResponse
    res
  }

  private val _params = TrieMap[String, Seq[String]]()
  private def _scalatraParams = new ScalatraParams(_params.toMap)
  override def params(implicit request: HttpServletRequest) = _scalatraParams

  def prepareParams(params: (String, String)*) = {
    _params ++= params.map { case (k, v) => k -> Seq(v) }
  }

  var renderCall: Option[RenderCall] = None
  override def render(path: String)(implicit format: Format = Format.HTML): String = {
    // If Content-Type is already set, never overwrite it.
    if (contentType == null) {
      contentType = format.contentType + charset.map(c => s"; charset=${c}").getOrElse("")
    }
    renderCall = Option(RenderCall(path))
    "Valid response body won't be returned from MockController. " +
      "When you'd like to verify response body, use Scalatra tests with embedded Jetty instead."
  }

  // initialize this controller

  initializeRequestScopeAttributes

}