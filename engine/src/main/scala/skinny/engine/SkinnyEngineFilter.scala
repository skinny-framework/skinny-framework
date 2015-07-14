package skinny.engine

import javax.servlet._
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.context.SkinnyEngineContext
import skinny.engine.util.UriDecoder

import scala.util.DynamicVariable

/**
 * An implementation of the SkinnyEngine DSL in a filter.  You may prefer a filter
 * to a SkinnyEngineServlet if:
 *
 * $ - you are sharing a URL space with another servlet or filter and want to
 *     delegate unmatched requests.  This is very useful when migrating
 *     legacy applications one page or resource at a time.
 *
 *
 * Unlike a SkinnyEngineServlet, does not send 404 or 405 errors on non-matching
 * routes.  Instead, it delegates to the filter chain.
 *
 * If in doubt, extend SkinnyEngineServlet instead.
 *
 * @see SkinnyEngineServlet
 */
trait SkinnyEngineFilter extends Filter with SkinnyEngineServletBase {

  private[this] val _filterChain: DynamicVariable[FilterChain] = new DynamicVariable[FilterChain](null)

  protected def filterChain: FilterChain = _filterChain.value

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    val httpRequest = request.asInstanceOf[HttpServletRequest]
    val httpResponse = response.asInstanceOf[HttpServletResponse]

    _filterChain.withValue(chain) {
      handle(httpRequest, httpResponse)
    }
  }

  // What goes in servletPath and what goes in pathInfo depends on how the underlying servlet is mapped.
  // Unlike the SkinnyEngine servlet, we'll use both here by default.  Don't like it?  Override it.
  override def requestPath(implicit ctx: SkinnyEngineContext): String = {
    val request = ctx.request
    def getRequestPath: String = request.getRequestURI match {
      case requestURI: String =>
        var uri = requestURI
        if (request.getContextPath.length > 0) uri = uri.substring(request.getContextPath.length)
        if (uri.length == 0) {
          uri = "/"
        } else {
          val pos = uri.indexOf(';')
          if (pos >= 0) uri = uri.substring(0, pos)
        }
        UriDecoder.firstStep(uri)
      case null => "/"
    }

    request.get("skinny.engine.SkinnyEngineFilter.requestPath") match {
      case Some(uri) => uri.toString
      case _ => {
        val requestPath = getRequestPath
        request.setAttribute("skinny.engine.SkinnyEngineFilter.requestPath", requestPath)
        requestPath.toString
      }
    }
  }

  override protected def routeBasePath(implicit ctx: SkinnyEngineContext): String = {
    if (ctx.servletContext == null) {
      throw new IllegalStateException("routeBasePath requires an initialized servlet context to determine the context path")
    }
    ctx.servletContext.getContextPath
  }

  protected var doNotFound: Action = () => filterChain.doFilter(mainThreadRequest, mainThreadResponse)

  methodNotAllowed { _ => filterChain.doFilter(mainThreadRequest, mainThreadResponse) }

  type ConfigT = FilterConfig

  // see Initializable.initialize for why
  def init(filterConfig: FilterConfig): Unit = {
    initialize(filterConfig)
  }

  def destroy: Unit = {
    shutdown()
  }

}
