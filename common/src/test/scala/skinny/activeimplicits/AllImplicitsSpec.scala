package skinny.activeimplicits

import org.scalatest._

class AllImplicitsSpec extends FlatSpec with Matchers {

  object Sample extends AllImplicits {
    def duration = 100.seconds
    def weeks = 4.weeks
    def removeFoo(str: String) = str.remove("foo")
  }

  it should "be available" in {
    import AllImplicits._
    Sample.duration should equal(100.seconds)
    Sample.weeks should equal(4.weeks)
    Sample.removeFoo("foo bar") should equal(" bar")
  }

}
