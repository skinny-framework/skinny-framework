package skinny.activeimplicits

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * ActiveSupport-ish implicit conversions.
 */
object NumberImplicits extends NumberImplicits

trait NumberImplicits {

  val KILOBYTE: BigDecimal = 1024L
  val MEGABYTE: BigDecimal = KILOBYTE * 1024L
  val GIGABYTE: BigDecimal = MEGABYTE * 1024L
  val TERABYTE: BigDecimal = GIGABYTE * 1024L
  val PETABYTE: BigDecimal = TERABYTE * 1024L
  val EXABYTE: BigDecimal = PETABYTE * 1024L

  case class RichNumber(num: Number) extends DurationConversions {

    override protected def durationIn(unit: TimeUnit) = {
      Duration(num.doubleValue(), unit) match {
        case f: FiniteDuration => f
        case _ => throw new IllegalArgumentException("Duration DSL not applicable to " + num)
      }
    }

    // byte()
    // bytes()

    def byte = bytes
    def bytes = num

    // kilobyte()
    // kilobytes()

    def kilobyte = kilobytes
    def kilobytes = num.doubleValue() * KILOBYTE

    // megabyte()
    // megabytes()

    def megabyte = megabytes
    def megabytes = num.doubleValue() * MEGABYTE

    // gigabyte()
    // gigabytes()

    def gigabyte = gigabytes
    def gigabytes = num.doubleValue() * GIGABYTE

    // terabyte()
    // terabytes()

    def terabyte = terabytes
    def terabytes = num.doubleValue() * TERABYTE

    // petabyte()
    // petabytes()

    def petabyte = petabytes
    def petabytes = num.doubleValue() * PETABYTE

    // exabyte()
    // exabytes()

    def exabyte = exabytes
    def exabytes = num.doubleValue() * EXABYTE

    // second()
    // seconds()

    // minute()
    // minutes()

    // hour()
    // hours()

    // day()
    // days()

    // duplicable?()

    // fortnight()
    // fortnights()

    // html_safe?()

    // in_milliseconds()

    // to_formatted_s(format = :default, options = {})

    // week()
    // weeks()

    def week: Duration = weeks

    def weeks: Duration = Duration(num.doubleValue() * 7, TimeUnit.DAYS)

  }

  implicit def fromIntToRichNumber(num: Int): RichNumber = RichNumber(num)
  implicit def fromLongToRichNumber(num: Long): RichNumber = RichNumber(num)
  implicit def fromDoubleToRichNumber(num: Double): RichNumber = RichNumber(num)
  implicit def fromBigDecimalToRichNumber(num: BigDecimal): RichNumber = RichNumber(num)

  // Duration implicits from scala.concurrent.duration

  implicit def pairIntToDuration(p: (Int, TimeUnit)): Duration = Duration(p._1.toLong, p._2)
  implicit def pairLongToDuration(p: (Long, TimeUnit)): FiniteDuration = Duration(p._1, p._2)
  implicit def durationToPair(d: Duration): (Long, TimeUnit) = (d.length, d.unit)

}
