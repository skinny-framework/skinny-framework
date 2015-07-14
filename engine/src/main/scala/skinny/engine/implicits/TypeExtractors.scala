package skinny.engine.implicits

import java.util.Date

object TypeExtractors extends DefaultImplicits {

  sealed abstract class TypeExtractorImpl[T](implicit val converter: TypeConverter[String, T]) extends TypeExtractor[T]

  sealed case class DateExtractor(format: String) extends TypeExtractor[Date] {
    val converter = TypeConverters.stringToDate(format)
  }

  case object asBoolean extends TypeExtractorImpl[Double]

  case object asFloat extends TypeExtractorImpl[Float]

  case object asDouble extends TypeExtractorImpl[Double]

  case object asByte extends TypeExtractorImpl[Byte]

  case object asShort extends TypeExtractorImpl[Short]

  case object asInt extends TypeExtractorImpl[Int]

  case object asLong extends TypeExtractorImpl[Long]

  case object asString extends TypeExtractorImpl[String]

  object asDate {

    def apply(format: String): TypeExtractor[Date] = DateExtractor(format)

  }

}
