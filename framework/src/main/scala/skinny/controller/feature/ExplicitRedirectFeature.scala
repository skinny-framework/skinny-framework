package skinny.controller.feature

import skinny.micro.SkinnyMicroBase
import skinny.micro.base.{ RedirectionDsl, SkinnyContextInitializer, UrlGenerator }

/**
  * Explicit redirect method support.
  */
trait ExplicitRedirectFeature extends RedirectionDsl with UrlGenerator with SkinnyContextInitializer {
  self: SkinnyMicroBase =>

}
