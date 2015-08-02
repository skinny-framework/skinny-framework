package skinny.engine

import javax.servlet.http.HttpServlet

/**
 * Async skinny-engine servlet.
 */
trait AsyncSkinnyEngineServlet
    extends HttpServlet
    with SkinnyEngineServletBase
    with AsyncFeatures {

}
