package skinny.validator

import org.scalatest._

class timeFormatSpec extends FlatSpec with Matchers {

  behavior of "timeFormat"

  it should "be available" in {
    val validate = timeFormat
    validate.name should equal("timeFormat")

    validate(param("x" -> null)).isSuccess should equal(true)

    validate(param("x" -> "2013-01-02 03:04:05")).isSuccess should equal(true)

    validate(param("x" -> "2013-01-02")).isSuccess should equal(false)
    validate(param("x" -> "2013-01-02 03")).isSuccess should equal(false)
    validate(param("x" -> "2013-01-02 03:04")).isSuccess should equal(true)

    validate(param("x" -> "01:02:03")).isSuccess should equal(true)

    validate(param("x" -> "2013-1-2 3:4:5")).isSuccess should equal(true)
    validate(param("x" -> "2013/01/02 12:34:56")).isSuccess should equal(true)
    validate(param("x" -> "2013/1/2 12-34-56")).isSuccess should equal(true)
    validate(param("x" -> "2013-01-02 123456")).isSuccess should equal(false)
  }

}
