package skinny

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.util.Locale

class I18nSpec extends FlatSpec with ShouldMatchers {

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

}
