package skinny.routing

import org.scalatra._
import skinny._
import skinny.routing.implicits.RoutesAsImplicits

trait Routes { self: ScalatraBase with SkinnyControllerBase with RoutesAsImplicits =>

  implicit val skinnyController: SkinnyControllerBase = this

}

