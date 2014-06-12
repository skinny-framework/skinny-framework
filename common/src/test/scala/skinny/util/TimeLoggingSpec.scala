package skinny.util

import org.scalatest._

class TimeLoggingSpec extends FlatSpec with Matchers with TimeLogging {

  behavior of "TimeLogging"

  it should "work" in {
    val result = warnElapsedTime(10) {
      Thread.sleep(100)
      "AAA"
    }
    result should equal("AAA")
  }

}
