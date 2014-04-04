package skinny

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ParamTypeSpec extends FlatSpec with ShouldMatchers {

  behavior of "ParamType.Boolean"

  it should "convert raw value to expected type" in {
    ParamType.Boolean.unapply("true") should equal(Some(true))
    ParamType.Boolean.unapply("false") should equal(Some(false))
    ParamType.Boolean.unapply(true) should equal(Some(true))
    ParamType.Boolean.unapply(false) should equal(Some(false))
    ParamType.Boolean.unapply("") should equal(Some(false))
    ParamType.Boolean.unapply(null) should equal(Some(false))
  }

  behavior of "ParamType.Double"

  it should "convert raw value to expected type" in {
    ParamType.Double.unapply("0.123") should equal(Some(0.123D))
    ParamType.Double.unapply("1.23") should equal(Some(1.23D))
    ParamType.Double.unapply(0.123D) should equal(Some(0.123D))
    ParamType.Double.unapply(1.23D) should equal(Some(1.23D))
    ParamType.Double.unapply(0.123F) should equal(None)
    ParamType.Double.unapply(1.23F) should equal(None)
    intercept[NumberFormatException] { ParamType.Double.unapply("aaa") }
    ParamType.Double.unapply(null) should equal(None)
  }

  behavior of "ParamType.Float"

  it should "convert raw value to expected type" in {
    ParamType.Float.unapply("0.123") should equal(Some(0.123F))
    ParamType.Float.unapply("1.23") should equal(Some(1.23F))
    ParamType.Float.unapply(0.123F) should equal(Some(0.123F))
    ParamType.Float.unapply(1.23F) should equal(Some(1.23F))
    ParamType.Float.unapply(0.123D) should equal(None)
    ParamType.Float.unapply(0.123D) should equal(None)
    ParamType.Float.unapply(1.23D) should equal(None)
    intercept[NumberFormatException] { ParamType.Float.unapply("aaa") }
    ParamType.Float.unapply(null) should equal(None)
  }

  behavior of "ParamType.Long"

  it should "convert raw value to expected type" in {
    ParamType.Long.unapply("-12345") should equal(Some(-12345))
    ParamType.Long.unapply("12345") should equal(Some(12345))
    ParamType.Long.unapply(12345) should equal(Some(12345))
    ParamType.Long.unapply(Long.MinValue) should equal(Some(Long.MinValue))
    ParamType.Long.unapply(Long.MaxValue) should equal(Some(Long.MaxValue))
    intercept[NumberFormatException] { ParamType.Long.unapply("aaa") }
    intercept[NumberFormatException] { ParamType.Long.unapply("123.45") }
    ParamType.Long.unapply(123.45) should equal(None)
    ParamType.Long.unapply(null) should equal(None)
  }

  behavior of "ParamType.Int"

  it should "convert raw value to expected type" in {
    ParamType.Int.unapply("-12345") should equal(Some(-12345))
    ParamType.Int.unapply("12345") should equal(Some(12345))
    ParamType.Int.unapply(12345) should equal(Some(12345))
    ParamType.Int.unapply(Int.MinValue) should equal(Some(Int.MinValue))
    ParamType.Int.unapply(Int.MaxValue) should equal(Some(Int.MaxValue))
    ParamType.Int.unapply(Int.MinValue.toLong) should equal(Some(Int.MinValue))
    ParamType.Int.unapply(Int.MaxValue.toLong) should equal(Some(Int.MaxValue))
    ParamType.Int.unapply(Int.MinValue.toLong - 1) should equal(None)
    ParamType.Int.unapply(Int.MaxValue.toLong + 1) should equal(None)
    ParamType.Int.unapply(123.45) should equal(None)
    ParamType.Int.unapply(Long.MaxValue) should equal(None)
    intercept[NumberFormatException] { ParamType.Int.unapply("aaa") }
    intercept[IllegalArgumentException] { ParamType.Int.unapply("123.45") }
    ParamType.Int.unapply(null) should equal(None)
  }

  behavior of "ParamType.Short"

  it should "convert raw value to expected type" in {
    ParamType.Short.unapply("-12345") should equal(Some(-12345))
    ParamType.Short.unapply("12345") should equal(Some(12345))
    ParamType.Short.unapply(12345) should equal(Some(12345))
    ParamType.Short.unapply(Short.MinValue) should equal(Some(Short.MinValue))
    ParamType.Short.unapply(Short.MaxValue) should equal(Some(Short.MaxValue))
    ParamType.Short.unapply(Short.MinValue.toInt) should equal(Some(Short.MinValue))
    ParamType.Short.unapply(Short.MaxValue.toInt) should equal(Some(Short.MaxValue))
    ParamType.Short.unapply(Short.MinValue.toLong) should equal(Some(Short.MinValue))
    ParamType.Short.unapply(Short.MaxValue.toLong) should equal(Some(Short.MaxValue))
    ParamType.Short.unapply(Short.MinValue.toInt - 1) should equal(None)
    ParamType.Short.unapply(Short.MaxValue.toInt + 1) should equal(None)
    ParamType.Short.unapply(123.45) should equal(None)
    ParamType.Short.unapply(Long.MaxValue) should equal(None)
    intercept[NumberFormatException] { ParamType.Short.unapply("aaa") }
    intercept[IllegalArgumentException] { ParamType.Short.unapply("123.45") }
    ParamType.Short.unapply(null) should equal(None)
  }

  behavior of "ParamType.BigDecimal"

  it should "convert raw value to expected type" in {
    ParamType.BigDecimal.unapply("-12345") should equal(Some(BigDecimal("-12345")))
    ParamType.BigDecimal.unapply("12345") should equal(Some(BigDecimal("12345")))
    ParamType.BigDecimal.unapply(12345) should equal(Some(BigDecimal("12345")))
    ParamType.BigDecimal.unapply(12345L) should equal(Some(BigDecimal("12345")))
    ParamType.BigDecimal.unapply(123.45D) should equal(Some(BigDecimal("123.45")))
    intercept[NumberFormatException] { ParamType.BigDecimal.unapply("aaa") }
    ParamType.BigDecimal.unapply(null) should equal(None)
  }

  behavior of "ParamType.String"

  it should "convert raw value to expected type" in {
    ParamType.String.unapply("abc") should equal(Some("abc"))
    ParamType.String.unapply(123) should equal(Some("123"))
    ParamType.String.unapply(123L) should equal(Some("123"))
    ParamType.String.unapply(1.23D) should equal(Some("1.23"))
    ParamType.String.unapply(1.23F) should equal(Some("1.23"))
    ParamType.String.unapply(null) should equal(None)
  }

  behavior of "ParamType.Byte"

  it should "convert raw value to expected type" in {
    val b: Byte = 123
    ParamType.Byte.unapply(b) should equal(Some(b))
    ParamType.Byte.unapply("123") should equal(Some(b))
    ParamType.Byte.unapply(123) should equal(None)
    ParamType.Byte.unapply(null) should equal(None)
  }

  behavior of "ParamType.ByteArray"

  it should "convert raw value to expected type" in {
    val bs: Array[Byte] = Array[Byte](123.toByte, 234.toByte, 345.toByte)
    ParamType.ByteArray.unapply(bs) should equal(Some(bs))
    ParamType.ByteArray.unapply(123) should equal(None)
    ParamType.ByteArray.unapply(null) should equal(None)
  }

  behavior of "ParamType.DateTime"

  it should "convert raw value to expected type" in {
    import org.joda.time.{ DateTime => JDateTime }
    val expected = JDateTime.parse("2013-01-02T03:04:05").getMillis
    ParamType.DateTime.unapply("2013-01-02T03:04:05").map(_.asInstanceOf[JDateTime].getMillis) should equal(Some(expected))
    intercept[IllegalArgumentException] { ParamType.DateTime.unapply("abc") }
    ParamType.DateTime.unapply(null) should equal(None)
  }

  behavior of "ParamType.LocalDate"

  it should "convert raw value to expected type" in {
    import org.joda.time.{ LocalDate => JLocalDate }
    val expected = JLocalDate.parse("2013-01-02").toString
    ParamType.LocalDate.unapply("2013-01-02").map(_.toString) should equal(Some(expected))
    ParamType.LocalDate.unapply("2013-01-02T03:04:05").map(_.toString) should equal(Some(expected))
    intercept[IllegalArgumentException] { ParamType.LocalDate.unapply("abc") }
    ParamType.LocalDate.unapply(null) should equal(None)
  }

  behavior of "ParamType.LocalTime"

  it should "convert raw value to expected type" in {
    import org.joda.time.{ LocalTime => JLocalTime }
    val expected = JLocalTime.parse("03:04:05").toString
    ParamType.LocalTime.unapply("03:04:05").map(_.toString) should equal(Some(expected))
    ParamType.LocalTime.unapply("2013-01-02T03:04:05").map(_.toString) should equal(Some(expected))
    intercept[IllegalArgumentException] { ParamType.LocalTime.unapply("abc") }
    ParamType.LocalTime.unapply(null) should equal(None)
  }

}
