package skinny.engine.implicits

import scala.language.implicitConversions

trait BigDecimalImplicits { self: DefaultImplicits =>

  implicit val stringToBigDecimal: TypeConverter[String, BigDecimal] = safe(BigDecimal(_))

  implicit val stringToSeqBigDecimal: TypeConverter[String, Seq[BigDecimal]] = stringToSeq(stringToBigDecimal)

}
