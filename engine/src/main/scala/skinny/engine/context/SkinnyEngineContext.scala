package skinny.engine.context

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.UnstableAccessValidation
import skinny.engine.implicits.{ CookiesImplicits, ServletApiImplicits, SessionImplicits }
import skinny.engine.request.StableHttpServletRequest

object SkinnyEngineContext {

  private class StableSkinnyEngineContext(
      implicit val request: HttpServletRequest,
      val response: HttpServletResponse,
      val servletContext: ServletContext,
      val unstableAccessValidation: UnstableAccessValidation) extends SkinnyEngineContext {
  }

  def surelyStable(ctx: SkinnyEngineContext, validation: UnstableAccessValidation): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(ctx.request, validation), ctx.response, ctx.servletContext, validation)
  }

  def build(ctx: ServletContext, req: HttpServletRequest, resp: HttpServletResponse, validation: UnstableAccessValidation): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(req, validation), resp, ctx, validation)
  }

  def buildWithRequest(req: HttpServletRequest, validation: UnstableAccessValidation): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(req, validation), null, null, validation)
  }

  def buildWithoutResponse(req: HttpServletRequest, ctx: ServletContext, validation: UnstableAccessValidation): SkinnyEngineContext = {
    new StableSkinnyEngineContext()(StableHttpServletRequest(req, validation), null, ctx, validation)
  }

}

trait SkinnyEngineContext
    extends ServletApiImplicits
    with SessionImplicits
    with CookiesImplicits {

  val request: HttpServletRequest

  val response: HttpServletResponse

  val servletContext: ServletContext

  val unstableAccessValidation: UnstableAccessValidation

  def surelyStable(validation: UnstableAccessValidation): SkinnyEngineContext = {
    SkinnyEngineContext.surelyStable(this, validation)
  }

}
