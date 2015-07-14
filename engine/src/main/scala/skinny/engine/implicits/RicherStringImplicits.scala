package skinny.engine.implicits

import scala.language.implicitConversions

trait RicherStringImplicits {

  implicit def stringToRicherString(s: String): RicherString = new RicherString(s)

}

object RicherStringImplicits extends RicherStringImplicits