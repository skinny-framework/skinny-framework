package skinny.engine.implicits

import scala.language.implicitConversions

/**
 * Implicit conversion for BigDecimal values.
 */
trait BigDecimalImplicits { self: DefaultImplicits =>

  implicit val stringToBigDecimal: TypeConverter[String, BigDecimal] = safe(BigDecimal(_))

  implicit val stringToSeqBigDecimal: TypeConverter[String, Seq[BigDecimal]] = stringToSeq(stringToBigDecimal)

}
