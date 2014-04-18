package skinny.test

import skinny._

/**
 * Mock of SkinnyServlet.
 */
trait MockServlet extends MockControllerBase with MockWebPageControllerFeatures { self: SkinnyServlet =>

}