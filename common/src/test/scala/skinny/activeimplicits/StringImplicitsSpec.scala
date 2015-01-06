package skinny.activeimplicits

import org.scalatest._

// http://api.rubyonrails.org/classes/String.html
class StringImplicitsSpec extends FlatSpec with Matchers with StringImplicits {

  it should "have #remove" in {
    "Hello Hello HELLO World!".remove("Hello") should equal("  HELLO World!")
  }

  it should "have #squish" in {
    " \n  foo\n\r \t bar \n".squish should equal("foo bar")
  }

  it should "have #at" in {
    "hello".at(0) should equal("h")
    "hello".at(1 to 3) should equal("ell")
    "hello".at(-2) should equal("l")
    "hello".at(-2 to -1) should equal("lo")
    "hello".at(5) should equal(null)
    "hello".at(5 to -1) should equal("")
  }

  it should "have #blank" in {
    "".blank should equal(true)
    "   ".blank should equal(true)
    " \t \n \r".blank should equal(true)
    " hello ".blank should equal(false)
  }

  it should "have #camelize / #lowerCamelize" in {
    "active_record".camelize should equal("ActiveRecord")
    "active_record/errors".camelize should equal("active_record.Errors")
    "active_record".lowerCamelize should equal("activeRecord")
    "active_record/errors".lowerCamelize should equal("active_record.errors")

    "active_record".camelizeAsRuby should equal("ActiveRecord")
    "active_record".lowerCamelizeAsRuby should equal("activeRecord")
    "active_record/errors".camelizeAsRuby should equal("ActiveRecord::Errors")
    "active_record/errors".lowerCamelizeAsRuby should equal("activeRecord::Errors")

    "active_record".camelizeAsScala should equal("ActiveRecord")
    "active_record/errors".camelizeAsScala should equal("active_record.Errors")
    "active_record".lowerCamelizeAsScala should equal("activeRecord")
    "active_record/errors".lowerCamelizeAsScala should equal("active_record.errors")
  }

  it should "have #dasherize" in {
    // 'puni_puni'.dasherize # => "puni-puni"
    "puni_puni".dasherize should equal("puni-puni")
  }

  it should "have #exclude" in {
    "hello".exclude("lo") should equal(false)
    "hello".exclude("ol") should equal(true)
    "hello".exclude("h") should equal(false)
  }

  it should "have #first" in {
    "hello".first should equal("h")
    "hello".first(1) should equal("h")
    "hello".first(2) should equal("he")
    "hello".first(0) should equal("")
    "hello".first(6) should equal("hello")
  }

  it should "have #from" in {
    "hello".from(0) should equal("hello")
    "hello".from(3) should equal("lo")
    "hello".from(-2) should equal("lo")
  }

  it should "have #to" in {
    "hello".to(0) should equal("h")
    "hello".to(3) should equal("hell")
    "hello".to(-2) should equal("hell")
  }

  it should "have #fromTo" in {
    "hello".fromTo(0, -1) should equal("hello")
    "hello".fromTo(1, -2) should equal("ell")
  }

  it should "have #parameterize" in {
    "Donald E. Knuth".parameterize should equal("donald-e-knuth")
  }

  //  it should "have #tableize" in {
  //    "RawScaledScorer".tableize should equal("raw_scaled_scorers")
  //    "egg_and_ham".tableize should equal("egg_and_hams")
  //    "fancyCategory".tableize should equal("fancy_categories")
  //  }

  it should "have #titleize" in {
    "man from the boondocks".titleize should equal("Man From The Boondocks")
    "x-men: the last stand".titleize should equal("X Men: The Last Stand")
  }

  it should "have #truncate" in {
    "Once upon a time in a world far far away".truncate(27) should equal(
      "Once upon a time in a wo...")
    "Once upon a time in a world far far away".truncate(27, " ") should equal(
      "Once upon a time in a...")
    "And they found that many people were sleeping better.".truncate(25, omission = "... (continued)") should equal(
      "And they f... (continued)")
  }

  it should "have #truncate_words" in {
    "Once upon a time in a world far far away".truncateWords(4) should equal(
      "Once upon a time...")
    "Once<br>upon<br>a<br>time<br>in<br>a<br>world".truncateWords(5, separator = "<br>") should equal(
      "Once<br>upon<br>a<br>time<br>in...")
    "And they found that many people were sleeping better.".truncateWords(5, omission = "... (continued)") should equal(
      "And they found that many... (continued)")
  }

  it should "hava #underscore" in {
    "ActiveModel".underscore should equal("active_model")
    "ActiveModel::Errors".underscore should equal("active_model/errors")

    "ActiveModel".underscore should equal("active_model")
    "activeModel.Errors".underscore should equal("active_model/errors")
    "ActiveModel.Errors".underscore should equal("active_model/errors")
    "active_model.Errors".underscore should equal("active_model/errors")
  }

}
