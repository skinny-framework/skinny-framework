package skinny.controller.feature

import skinny.micro.SkinnyMicroBase

/**
 * X-XSS-Protection header support
 *
 * - https://www.owasp.org/index.php/List_of_useful_HTTP_headers
 */
trait AsyncXXSSProtectionHeaderFeature { self: SkinnyMicroBase with AsyncBeforeAfterActionFeature =>

  // NOTE: for all HTML responses defined as Skinny routes
  beforeAction() { implicit ctx =>
    response(context).setHeader("X-XSS-Protection", "1; mode=block")
  }

}
