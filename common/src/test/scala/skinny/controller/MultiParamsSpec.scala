package skinny.controller

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MultiParamsSpec extends AnyFlatSpec with Matchers {

  behavior of "MultiParams"

  it should "be available" in {
    val params = MultiParams(Map("foo" -> Seq("123", "abc"), "bar" -> Nil))

    params.foo should equal(Seq("123", "abc"))
    params.bar should equal(Nil)
    params.baz should equal(Nil)
  }

}
