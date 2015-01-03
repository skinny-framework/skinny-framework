package skinny.controller

import org.scalatest._

class ParamsSpec extends FlatSpec with Matchers {

  behavior of "Params"

  it should "work with nullable values" in {
    // Some(null) must be avoided
    {
      val params = Params(Map("something" -> "foo"))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(Some("foo"))

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> null))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(None)

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> Some("foo")))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(Some("foo"))

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> Some(Some("foo"))))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(Some(Some("foo")))

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> Some(null)))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(None)

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> None))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(None)

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
  }

}
