package skinny.mailer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BodyTypeSpec extends AnyFlatSpec with Matchers {

  it should "have Text type" in {
    Text.extension should equal("text")
  }

  it should "have Html type" in {
    Html.extension should equal("html")
  }

}
