package skinny.controller.feature

import skinny.engine.SkinnyScalatraBase

/**
 * X-Content-Type-Options header support.
 *
 * - https://blogs.msdn.com/b/ie/archive/2008/09/02/ie8-security-part-vi-beta-2-update.aspx?Redirected=true
 * - http://msdn.microsoft.com/en-us/library/ie/gg622941(v=vs.85).aspx
 * - https://github.com/blog/1482-heads-up-nosniff-header-support-coming-to-chrome-and-firefox
 * - https://www.owasp.org/index.php/List_of_useful_HTTP_headers
 */
trait XContentTypeOptionsNosniffHeaderFeature { self: SkinnyScalatraBase with BeforeAfterActionFeature =>

  // NOTE: To force this header to all responses
  //beforeAction() {
  before() {
    response.setHeader("X-Content-Type-Options", "nosniff")
  }

}
