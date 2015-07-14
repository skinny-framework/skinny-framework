package skinny.engine.context

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse, HttpServletResponseWrapper }

import skinny.engine.ApiFormats
import skinny.engine.implicits.{ CookiesImplicits, ServletApiImplicits, SessionImplicits }
import skinny.engine.request.StableHttpServletRequest
import skinny.engine.response.ResponseStatus

object SkinnyEngineContext {

  private class StableSkinnyEngineContext(
      implicit val request: HttpServletRequest,
      val response: HttpServletResponse,
      val servletContext: ServletContext) extends SkinnyEngineContext {

    val readOnlyRequest: StableHttpServletRequest = new StableHttpServletRequest(request)
  }

  def toStable(ctx: SkinnyEngineContext): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(ctx.request), ctx.response, ctx.servletContext)
  }

  def build()(
    implicit servletContext: ServletContext, request: HttpServletRequest, response: HttpServletResponse): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(request, response, servletContext)
  }

  def buildWithRequest(request: HttpServletRequest): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(request, null, null)
  }

}

trait SkinnyEngineContext
    extends ServletApiImplicits
    with SessionImplicits
    with CookiesImplicits {

  val request: HttpServletRequest

  val readOnlyRequest: StableHttpServletRequest

  val response: HttpServletResponse

  val servletContext: ServletContext

  def toStable(): SkinnyEngineContext = SkinnyEngineContext.toStable(this)

}
