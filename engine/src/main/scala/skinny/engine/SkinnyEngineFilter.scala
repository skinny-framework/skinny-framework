package skinny.engine

import javax.servlet._
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.base.MainThreadLocalEverywhere
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.routing.RoutingDsl
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
trait SkinnyEngineFilter
    extends Filter
    with SkinnyEngineFilterBase
    with ThreadLocalFeatures {

}
