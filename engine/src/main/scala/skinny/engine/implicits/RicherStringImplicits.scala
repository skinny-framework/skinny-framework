package skinny.engine.implicits

import scala.language.implicitConversions

/**
 * Implicit conversions for String values.
 */
trait RicherStringImplicits {

  implicit def stringToRicherString(s: String): RicherString = new RicherString(s)

}

object RicherStringImplicits extends RicherStringImplicits
