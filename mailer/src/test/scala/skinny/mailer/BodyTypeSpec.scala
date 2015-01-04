package skinny.mailer

import org.scalatest._

class BodyTypeSpec extends FlatSpec with Matchers {

  it should "have Text type" in {
    Text.extension should equal("text")
  }

  it should "have Html type" in {
    Html.extension should equal("html")
  }

}
