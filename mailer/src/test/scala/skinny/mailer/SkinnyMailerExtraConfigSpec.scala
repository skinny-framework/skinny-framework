package skinny.mailer

import org.scalatest._

class SkinnyMailerExtraConfigSpec extends FlatSpec with Matchers {

  it should "be available" in {
    val config = SkinnyMailerExtraConfig.apply("foo" -> "bar")
    config.properties.get("foo") should equal(Some("bar"))
    config.properties.get("baz") should equal(None)
  }

}
