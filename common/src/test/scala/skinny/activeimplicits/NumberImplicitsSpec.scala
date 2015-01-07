package skinny.activeimplicits

import org.scalatest._

class NumberImplicitsSpec extends FlatSpec with Matchers with NumberImplicits {

  it should "have duration implicits" in {
    1.second.toMillis should equal(1000L)
    2000.millis should equal(2.seconds)

    1.minute should equal(60.seconds)
    1.hour should equal(60.minutes)
    1.day should equal(24.hours)
    1.week should equal(7.days)

    2.minutes should equal(120.seconds)
    2.hours should equal(120.minutes)
    2.days should equal(48.hours)
    2.weeks should equal(14.days)
  }

  it should "have #byte, #kilobytes ..." in {
    1.byte should equal(1)
    100.bytes should equal(100)

    0.5D.kilobytes should equal(512.0)
    1.kilobyte should equal(1024)
    100.kilobytes should equal(100L * KILOBYTE)

    1.megabyte should equal(MEGABYTE)
    100.megabytes should equal(100L * MEGABYTE)

    1.gigabyte should equal(GIGABYTE)
    100.gigabytes should equal(100L * GIGABYTE)

    1.terabyte should equal(TERABYTE)
    100.terabytes should equal(100L * TERABYTE)

    1.petabyte should equal(PETABYTE)
    100.petabytes should equal(100L * PETABYTE)

    1.exabyte should equal(EXABYTE)
    100.exabytes should equal(100L * EXABYTE)
  }

}
