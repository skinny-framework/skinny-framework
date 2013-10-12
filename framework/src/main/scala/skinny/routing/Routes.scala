package skinny.routing

import org.scalatra._
import skinny._
import skinny.routing.implicits.RoutesAsImplicits

/**
 * Route configurator for SkinnyController.
 *
 * When using this trait, Route can be a RichRoute, so you can call #as(Symbol) method now.
 */
trait Routes { self: ScalatraBase with SkinnyControllerBase with RoutesAsImplicits =>

  /**
   * Pass this controller instance implicitly
   * because [[skinny.routing.implicits.RoutesAsImplicits]] expects [[skinny.controller.SkinnyControllerBase]].
   */
  implicit val skinnyController: SkinnyControllerBase = this

}

