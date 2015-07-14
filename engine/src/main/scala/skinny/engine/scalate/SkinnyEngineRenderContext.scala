package skinny.engine.scalate

import java.io.PrintWriter
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse, HttpSession }

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.servlet.ServletRenderContext
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.csrf.{ XsrfTokenSupport, CsrfTokenSupport }
import skinny.engine.{ ApiFormats, MultiParams, Params, SkinnyEngineBase }
import skinny.engine.flash.FlashMapSupport
import skinny.engine.multipart.{ FileMultiParams, FileItem, FileUploadSupport }
import skinny.engine.routing.Route
import skinny.engine.util.UrlGenerator

/**
 * A render context integrated with SkinnyEngine.  Exposes a few extra standard bindings to the template.
 */
class SkinnyEngineRenderContext(
    protected val base: SkinnyEngineBase,
    implicit val context: SkinnyEngineContext,
    engine: TemplateEngine,
    out: PrintWriter,
    req: HttpServletRequest,
    res: HttpServletResponse) extends ServletRenderContext(engine, out, req, res, base.servletContext) {

  def flash: scala.collection.Map[String, Any] = base match {
    case flashMapSupport: FlashMapSupport => flashMapSupport.flash(context)
    case _ => Map.empty
  }

  def session: HttpSession = base.session(context)

  def sessionOption: Option[HttpSession] = base.sessionOption(context)

  def params: Params = base.params(context)

  def multiParams: MultiParams = base.multiParams(context)

  def format: String = base match {
    case af: ApiFormats => af.format(context)
    case _ => ""
  }

  def fileMultiParams: FileMultiParams = base match {
    case fu: FileUploadSupport => fu.fileMultiParams(context)
    case _ => new FileMultiParams()
  }

  def fileParams: scala.collection.Map[String, FileItem] = base match {
    case fu: FileUploadSupport => fu.fileParams(context)
    case _ => Map.empty
  }

  def csrfKey = base match {
    case csrfTokenSupport: CsrfTokenSupport => csrfTokenSupport.csrfKey
    case _ => ""
  }

  def csrfToken = base match {
    case csrfTokenSupport: CsrfTokenSupport => csrfTokenSupport.csrfToken(request)
    case _ => ""
  }
  def xsrfKey = base match {
    case csrfTokenSupport: XsrfTokenSupport => csrfTokenSupport.xsrfKey
    case _ => ""
  }

  def xsrfToken = base match {
    case csrfTokenSupport: XsrfTokenSupport => csrfTokenSupport.xsrfToken(request)
    case _ => ""
  }

  /**
   * Calculate a URL for a reversible route and some params.
   *
   * @param route a reversible route
   * @param params a list of named param/value pairs
   * @return a URI that matches the route for the given params
   * @throws Exception if the route is not reversible
   * @throws IllegalStateException if the route's base path cannot be
   * determined.  This may occur outside of an HTTP request's lifecycle.
   */
  def url(route: Route, params: (String, String)*): String = {
    UrlGenerator.url(route, params: _*)(context)
  }

  /**
   * Calculate a URL for a reversible route and some splats.
   *
   * @param route a reversible route
   * @param splat the first splat parameter
   * @param moreSplats any splat parameters beyond the first
   * @return a URI that matches the route for the given splats
   * @throws Exception if the route is not reversible
   * @throws IllegalStateException if the route's base path cannot be
   * determined.  This may occur outside of an HTTP request's lifecycle.
   */
  def url(route: Route, splat: String, moreSplats: String*): String = {
    UrlGenerator.url(route, splat, moreSplats: _*)(context)
  }

  /**
   * Calculate a URL for a reversible route, some params, and some splats.
   *
   * @param route a reversible route
   * @param params a map of param/value pairs
   * @param splats a series of splat parameters
   * @return a URI that matches the route for the given splats
   * @throws Exception if the route is not reversible
   * @throws IllegalStateException if the route's base path cannot be
   * determined.  This may occur outside of an HTTP request's lifecycle.
   */
  def url(
    route: Route,
    params: Map[String, String],
    splats: Iterable[String]): String = {
    UrlGenerator.url(route, params, splats)(context)
  }
}
