package skinny.engine

import javax.servlet._
import javax.servlet.http._

import skinny.engine.context.SkinnyEngineContext
import skinny.engine.implicits.{ RicherStringImplicits, ServletApiImplicits }
import skinny.engine.util.UriDecoder

import scala.util.control.Exception.catching

/**
 * An implementation of the SkinnyEngine DSL in a servlet.  This is the recommended
 * base trait for most SkinnyEngine applications.  Use a servlet if:
 *
 * $ - your SkinnyEngine routes run in a subcontext of your web application.
 * $ - you want SkinnyEngine to have complete control of unmatched requests.
 * $ - you think you want a filter just for serving static content with the
 *     default servlet; SkinnyEngineServlet can do this too
 * $ - you don't know the difference
 *
 * @see SkinnyEngineFilter
 */
trait SkinnyEngineServlet
    extends HttpServlet
    with SkinnyEngineBase
    with SkinnyEngineBasicFeatures {

  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    handle(request, response)
  }

  /**
   * Defines the request path to be matched by routers.  The default
   * definition is optimized for `path mapped` servlets (i.e., servlet
   * mapping ends in `&#47;*`).  The route should match everything matched by
   * the `&#47;*`.  In the event that the request URI equals the servlet path
   * with no trailing slash (e.g., mapping = `/admin&#47;*`, request URI =
   * '/admin'), a '/' is returned.
   *
   * All other servlet mappings likely want to return request.getServletPath.
   * Custom implementations are allowed for unusual cases.
   */
  override def requestPath(implicit ctx: SkinnyEngineContext): String = {
    SkinnyEngineServlet.requestPath(ctx.request)
  }

  override protected def routeBasePath(implicit ctx: SkinnyEngineContext): String = {
    require(config != null, "routeBasePath requires the servlet to be initialized")
    require(ctx.request != null, "routeBasePath requires an active request to determine the servlet path")

    servletContext.getContextPath + ctx.request.getServletPath
  }

  /**
   * Invoked when no route matches.  By default, calls `serveStaticResource()`,
   * and if that fails, calls `resourceNotFound()`.
   *
   * This action can be overridden by a notFound block.
   */
  protected var doNotFound: Action = () => {
    serveStaticResource()(skinnyEngineContext)
      .getOrElse(resourceNotFound()(skinnyEngineContext))
  }

  /**
   * Attempts to find a static resource matching the request path.
   * Override to return None to stop this.
   */
  protected def serveStaticResource()(
    implicit ctx: SkinnyEngineContext): Option[Any] = {
    servletContext.resource(ctx.request) map { _ =>
      servletContext.getNamedDispatcher("default").forward(ctx.request, ctx.response)
    }
  }

  /**
   * Called by default notFound if no routes matched and no static resource could be found.
   */
  protected def resourceNotFound()(
    implicit ctx: SkinnyEngineContext): Any = {
    ctx.response.setStatus(404)
    if (isDevelopmentMode) {
      val error = "Requesting \"%s %s\" on servlet \"%s\" but only have: %s"
      ctx.response.getWriter println error.format(
        ctx.request.getMethod,
        Option(ctx.request.getPathInfo) getOrElse "/",
        ctx.request.getServletPath,
        routes.entryPoints.mkString("<ul><li>", "</li><li>", "</li></ul>"))
    }
  }

  type ConfigT = ServletConfig

  override def init(config: ServletConfig): Unit = {
    super.init(config)
    initialize(config) // see Initializable.initialize for why
  }

  override def initialize(config: ServletConfig): Unit = {
    super.initialize(config)
  }

  override def destroy(): Unit = {
    shutdown()
    super.destroy()
  }

}

object SkinnyEngineServlet {

  import ServletApiImplicits._
  import RicherStringImplicits._

  val RequestPathKey = "skinny.engine.SkinnyEngineServlet.requestPath"

  def requestPath(request: HttpServletRequest): String = {
    require(request != null, "The request can't be null for getting the request path")
    def startIndex(r: HttpServletRequest) =
      r.getContextPath.blankOption.map(_.length).getOrElse(0) + r.getServletPath.blankOption.map(_.length).getOrElse(0)
    def getRequestPath(r: HttpServletRequest) = {
      val u = (catching(classOf[NullPointerException]) opt { r.getRequestURI } getOrElse "/")
      requestPath(u, startIndex(r))
    }

    request.get(RequestPathKey) map (_.toString) getOrElse {
      val rp = getRequestPath(request)
      request(RequestPathKey) = rp
      rp
    }
  }

  def requestPath(uri: String, idx: Int): String = {
    val u1 = UriDecoder.firstStep(uri)
    val u2 = (u1.blankOption map { _.substring(idx) } flatMap (_.blankOption) getOrElse "/")
    val pos = u2.indexOf(';')
    if (pos > -1) u2.substring(0, pos) else u2
  }

}
