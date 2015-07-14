package skinny.controller.feature

import skinny.controller.Flash
import skinny.engine.SkinnyEngineBase
import skinny.engine.flash.FlashMapSupport

/**
 * Easy-to-use Flash support.
 */
trait FlashFeature extends FlashMapSupport {

  self: SkinnyEngineBase with RequestScopeFeature =>

  // just set Flash object to request scope
  before() {
    if (requestScope.get(RequestScopeFeature.ATTR_FLASH).isEmpty) {
      set(RequestScopeFeature.ATTR_FLASH, Flash(flash))
    }
  }

}

