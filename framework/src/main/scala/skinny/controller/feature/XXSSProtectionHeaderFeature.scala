package skinny.controller.feature

import skinny.engine.SkinnyScalatraBase

/**
 * X-XSS-Protection header support
 *
 * - https://www.owasp.org/index.php/List_of_useful_HTTP_headers
 */
trait XXSSProtectionHeaderFeature { self: SkinnyScalatraBase with BeforeAfterActionFeature =>

  // NOTE: for all HTML responses defined as Skinny routes
  beforeAction() {
    response.setHeader("X-XSS-Protection", "1; mode=block")
  }

}
