package skinny.controller.feature

import skinny.controller.Flash
import skinny.micro.SkinnyMicroBase
import skinny.micro.base.BeforeAfterDsl
import skinny.micro.contrib.FlashMapSupport

/**
  * Easy-to-use Flash support.
  */
trait FlashFeature extends FlashMapSupport with BeforeAfterDsl { self: SkinnyMicroBase with RequestScopeFeature =>

  // just set Flash object to request scope
  before() {
    if (requestScope.get(RequestScopeFeature.ATTR_FLASH).isEmpty) {
      set(RequestScopeFeature.ATTR_FLASH, Flash(flash(context)))(context)
    }
  }

}
