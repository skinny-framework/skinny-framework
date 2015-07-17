package skinny.engine

import javax.servlet.http._

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
    with SkinnyEngineServletBase
    with ThreadLocalFeatures {

}
