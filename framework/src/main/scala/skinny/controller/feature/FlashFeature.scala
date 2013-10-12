package skinny.controller.feature

import skinny.controller.Flash
import org.scalatra._

/**
 * Easy-to-use Flash support.
 */
trait FlashFeature extends FlashMapSupport {

  self: org.scalatra.ScalatraBase with RequestScopeFeature =>

  // just set Flash object to request scope
  before() {
    if (requestScope("flash").isEmpty) {
      set("flash", Flash(flash))
    }
  }

}

