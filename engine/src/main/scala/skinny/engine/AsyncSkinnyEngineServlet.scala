package skinny.engine

import javax.servlet.http.HttpServlet

trait AsyncSkinnyEngineServlet
    extends HttpServlet
    with SkinnyEngineServletBase
    with AsyncFeatures {

}
