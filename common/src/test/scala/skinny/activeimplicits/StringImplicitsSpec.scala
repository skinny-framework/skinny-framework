package skinny.activeimplicits

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import skinny.nlp.SkinnyJapaneseAnalyzerFactory

// http://api.rubyonrails.org/classes/String.html
class StringImplicitsSpec extends AnyFlatSpec with Matchers with StringImplicits {

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
      "Once upon a time in a wo..."
    )
    "Once upon a time in a world far far away".truncate(27, " ") should equal(
      "Once upon a time in a..."
    )
    "And they found that many people were sleeping better.".truncate(25, omission = "... (continued)") should equal(
      "And they f... (continued)"
    )
  }

  it should "have #truncate_words" in {
    "Once upon a time in a world far far away".truncateWords(4) should equal(
      "Once upon a time..."
    )
    "Once<br>upon<br>a<br>time<br>in<br>a<br>world".truncateWords(5, separator = "<br>") should equal(
      "Once<br>upon<br>a<br>time<br>in..."
    )
    "And they found that many people were sleeping better.".truncateWords(5, omission = "... (continued)") should equal(
      "And they found that many... (continued)"
    )
    "treat null as empty string.".truncateWords(10, separator = null, omission = null) should equal(
      "treat null"
    )
  }

  it should "have #underscore" in {
    "ActiveModel".underscore should equal("active_model")
    "ActiveModel::Errors".underscore should equal("active_model/errors")

    "ActiveModel".underscore should equal("active_model")
    "activeModel.Errors".underscore should equal("active_model/errors")
    "ActiveModel.Errors".underscore should equal("active_model/errors")
    "active_model.Errors".underscore should equal("active_model/errors")
  }

  it should "have #katakana" in {
    "東京特許許可局".katakanaReadings should equal(Seq("トウキョウ", "トッキョ", "キョカ", "キョク"))
    "東京特許許可局".katakana should equal("トウキョウトッキョキョカキョク")

    "東京特許許可局".toKatakanaReadings should equal(Seq("トウキョウ", "トッキョ", "キョカ", "キョク"))
    "東京特許許可局".toKatakana should equal("トウキョウトッキョキョカキョク")

    "ようかい体操第一".katakana should equal("ヨウカイタイソウダイイチ")
    "妖怪タイソウ第１".katakana should equal("ヨウカイタイソウダイイチ")
    "ようかい体操第1".katakana should equal("ヨウカイタイソウダイ1")
    "日本マイクロソフト株式会社".katakana should equal("ニッポンマイクロソフトカブシキガイシャ")
    "型安全な Web フレームワーク".katakana should equal("カタアンゼンナwebフレームワーク")
    "地獄のミサワ".katakana should equal("ジゴクノミサワ")
    "椎名林檎".katakana should equal("シイナリンゴ")
    "rpscala".katakana should equal("rpscala")
    "区切り文字".katakana should equal("クギリモジ")
    "毎日深夜0時更新！掘り出し物をチェック".katakana should equal("マイニチシンヤ0ジコウシンホリダシモノヲチェック")
    "Skinny Framework はお好きなようにご利用ください".katakana should equal("skinnyframeworkハオスキナヨウニゴリヨウクダサイ")
  }

  it should "have #katakana with customized analyzer" in {
    val dictionaryText =
      """日本,日本,ニホン,カスタム名詞
        |Skinny Framework,スキニーフレームワーク,スキニーフレームワーク,カスタム名詞""".stripMargin
    implicit val analyzer = SkinnyJapaneseAnalyzerFactory.create(dictionaryText)

    "日本マイクロソフト株式会社".katakana should equal("ニホンマイクロソフトカブシキガイシャ")
    "Skinny Framework はお好きなようにご利用ください".katakana should equal("スキニーフレームワークハオスキナヨウニゴリヨウクダサイ")
  }

  it should "have #hiragana" in {
    "東京特許許可局".hiraganaReadings should equal(Seq("とうきょう", "とっきょ", "きょか", "きょく"))
    "東京特許許可局".hiragana should equal("とうきょうとっきょきょかきょく")

    "東京特許許可局".toHiraganaReadings should equal(Seq("とうきょう", "とっきょ", "きょか", "きょく"))
    "東京特許許可局".toHiragana should equal("とうきょうとっきょきょかきょく")

    "ようかい体操第一".hiragana should equal("ようかいたいそうだいいち")
    "妖怪タイソウ第１".hiragana should equal("ようかいたいそうだいいち")
    "ようかい体操第1".hiragana should equal("ようかいたいそうだい1")
    "日本マイクロソフト株式会社".hiragana should equal("にっぽんまいくろそふとかぶしきがいしゃ")
    "型安全な Web フレームワーク".hiragana should equal("かたあんぜんなwebふれーむわーく")
    "地獄のミサワ".hiragana should equal("じごくのみさわ")
    "椎名林檎".hiragana should equal("しいなりんご")
    "rpscala".hiragana should equal("rpscala")
    "区切り文字".hiragana should equal("くぎりもじ")
    "毎日深夜0時更新！掘り出し物をチェック".hiragana should equal("まいにちしんや0じこうしんほりだしものをちぇっく")
    "Skinny Framework はお好きなようにご利用ください".hiragana should equal("skinnyframeworkはおすきなようにごりようください")
  }

  it should "have #romaji" in {
    "東京特許許可局".romaji should equal("tokyotokkyokyokakyoku")
    "ようかい体操第一".romaji should equal("yokaitaisodaiichi")
    "妖怪タイソウ第１".romaji should equal("yokaitaisodaiichi")
    "ようかい体操第1".romaji should equal("yokaitaisodai1")
    "日本マイクロソフト株式会社".romaji should equal("nipponmaikurosofutokabushikigaisha")
    "型安全な Web フレームワーク".romaji should equal("kataanzennawebfuremuwaku")
    "地獄のミサワ".romaji should equal("jigokunomisawa")
    "椎名林檎".romaji should equal("shiinaringo")
    "rpscala".romaji should equal("rpscala")
    "区切り文字".romaji should equal("kugirimoji")
    "毎日深夜0時更新！掘り出し物をチェック".romaji should equal("mainichishin'ya0jikoshinhoridashimonoochekku")
    "Skinny Framework はお好きなようにご利用ください".romaji should equal("skinnyframeworkhaosukinayonigoriyokudasai")

    "東京特許許可局".toRomaji should equal("tokyotokkyokyokakyoku")
  }

  it should "have #romaji with custom analyzer" in {
    val dictionaryText =
      """日本,日本,nihon,カスタム名詞
        |マイクロソフト,マイクロソフト,microsoft,カスタム名詞
        |株式会社,K.K.,k.k.,カスタム名詞""".stripMargin
    implicit val analyzer = SkinnyJapaneseAnalyzerFactory.create(dictionaryText)

    "日本マイクロソフト株式会社".romajiReadings should equal(Seq("nihon", "microsoft", "k.k."))
    "日本マイクロソフト株式会社".romaji should equal("nihonmicrosoftk.k.")

    "日本マイクロソフト株式会社".toRomajiReadings should equal(Seq("nihon", "microsoft", "k.k."))
    "日本マイクロソフト株式会社".toRomaji should equal("nihonmicrosoftk.k.")
  }

}
