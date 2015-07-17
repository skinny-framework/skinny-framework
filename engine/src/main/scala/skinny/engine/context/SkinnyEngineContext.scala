package skinny.engine.context

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.implicits.{ CookiesImplicits, ServletApiImplicits, SessionImplicits }
import skinny.engine.request.StableHttpServletRequest

object SkinnyEngineContext {

  private class StableSkinnyEngineContext(
      implicit val request: HttpServletRequest,
      val response: HttpServletResponse,
      val servletContext: ServletContext) extends SkinnyEngineContext {
  }

  def surelyStable(ctx: SkinnyEngineContext): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(ctx.request), ctx.response, ctx.servletContext)
  }

  def build(ctx: ServletContext, req: HttpServletRequest, resp: HttpServletResponse): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(req), resp, ctx)
  }

  def buildWithRequest(req: HttpServletRequest): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(req), null, null)
  }

  def buildWithoutResponse(req: HttpServletRequest, ctx: ServletContext): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(req), null, ctx)
  }

}

trait SkinnyEngineContext
    extends ServletApiImplicits
    with SessionImplicits
    with CookiesImplicits {

  val request: HttpServletRequest

  val response: HttpServletResponse

  val servletContext: ServletContext

  def surelyStable: SkinnyEngineContext = SkinnyEngineContext.surelyStable(this)

}
