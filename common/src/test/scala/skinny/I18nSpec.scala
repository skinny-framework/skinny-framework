package skinny

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.util.Locale

class I18nSpec extends AnyFlatSpec with Matchers {

  behavior of "I18n"

  it should "be available" in {
    val i18n = I18n()
    i18n.get("name") should equal(Some("Name"))
    i18n.get("foo.name") should equal(Some("FooName"))
    i18n.get("foo.bar.baz") should equal(Some("Baz"))
  }

  it should "be available with locale" in {
    val i18n = I18n(new Locale("ja"))
    i18n.get("name") should equal(Some("名前"))
    i18n.get("foo.bar.baz") should equal(Some("バズ"))
  }

  it should "load user.name correctly" in {
    val i18n = I18n()
    i18n.get("user.name") should equal(Some("user-name"))
  }

}
