package skinny.controller.feature

import skinny.controller.Flash
import org.scalatra._

trait FlashFeature extends FlashMapSupport {
  self: org.scalatra.ScalatraBase with RequestScopeFeature =>

  before() {
    set("flash", Flash(flash))
  }

}

