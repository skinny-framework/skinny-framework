package skinny.routing

import skinny.controller.feature.SkinnyControllerCommonBase
import skinny.micro.SkinnyMicroBase
import skinny.routing.implicits.RoutesAsImplicits

/**
  * Route configurator for SkinnyController.
  *
  * When using this trait, Route can be a RichRoute, so you can call #as(String) method now.
  */
trait Routes { self: SkinnyMicroBase with SkinnyControllerCommonBase with RoutesAsImplicits =>

  /**
    * Pass this controller instance implicitly
    * because [[skinny.routing.implicits.RoutesAsImplicits]] expects [[skinny.controller.SkinnyControllerBase]].
    */
  implicit val skinnyController: SkinnyControllerCommonBase = this

}
