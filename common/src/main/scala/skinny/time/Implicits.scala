package skinny.time

import scala.language.implicitConversions

object Implicits extends Implicits

trait Implicits {

  @inline implicit def skinnyDateTimeInterpolationImplicitDef(s: StringContext) = {
    new skinny.time.DateTimeInterpolationString(s)
  }

}
