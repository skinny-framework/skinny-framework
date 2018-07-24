package skinny.test

import javax.servlet.http._
import javax.servlet.ServletContext
import org.json4s._
import org.mockito.Mockito._
import skinny.SkinnyEnv
import skinny.micro.{ SkinnyMicroParams, UnstableAccessValidation }
import skinny.micro.context.SkinnyContext
import skinny.json.JSONStringOps
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

  override def skipHaltingWhenRedirection = SkinnyEnv.isTest()

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

  override def request(implicit ctx: SkinnyContext = context): HttpServletRequest = mockRequest

  override def response(implicit ctx: SkinnyContext = context): HttpServletResponse = mockResponse

  override implicit def servletContext: ServletContext = mock(classOf[ServletContext])

  override implicit def skinnyContext(implicit ctx: ServletContext): SkinnyContext = {
    SkinnyContext.build(ctx, mockRequest, mockResponse, UnstableAccessValidation(true, false))
  }

  override def halt[T: Manifest](
      status: Integer = null,
      body: T = (),
      headers: Map[String, String] = Map.empty,
      reason: String = null
  ): Nothing = {

    throw new MockHaltException(
      status = Option(status).map(_.intValue()),
      reason = Option(reason),
      headers = headers,
      body = body
    )
  }

  def getOutputStreamContents: String = {
    response.getOutputStream.toString
  }

  def getOutputStreamContents(charset: String): String = {
    response.getOutputStream
      .asInstanceOf[MockServletOutputStream]
      .toString(charset)
  }

  private[this] val _params = TrieMap[String, Seq[String]]()

  override def params(implicit ctx: SkinnyContext) = {
    val mergedParams = (super.params(ctx) ++ new SkinnyMicroParams(_params.toMap)).mapValues(v => Seq(v))
    new SkinnyMicroParams(if (_parsedBody.isDefined) {
      getMergedMultiParams(mergedParams.toMap,
                           parsedBody(ctx).extract[Map[String, String]].mapValues(v => Seq(v)).toMap)
    } else {
      mergedParams.toMap
    })
  }

  def prepareParams(params: (String, String)*) = {
    params.foreach {
      case (k, v) =>
        _params += (k -> (_params.getOrElse(k, Seq[String]()) :+ v))
    }
  }

  override def multiParams(implicit ctx: SkinnyContext) = {
    _params.foldLeft(super.multiParams(ctx)) { (params, kv) =>
      params + (kv._1 -> params.get(kv._1).map(_ ++ kv._2).getOrElse(kv._2))
    }
  }

  private[this] var _parsedBody: Option[JValue] = None
  override def parsedBody(implicit ctx: SkinnyContext): JValue = {
    _parsedBody.getOrElse(JNothing)
  }

  def prepareJSONBodyRequest(json: String): Unit = {
    _parsedBody = JSONStringOps.fromJSONStringToJValue(json).toOption
  }

  // initialize this controller

  initializeRequestScopeAttributes(skinnyContext)

}
