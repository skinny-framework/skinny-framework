package skinny.nlp

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SkinnyJapaneseAnalyzerSpec extends AnyFlatSpec with Matchers {

  it should "be available" in {
    val analyzer = SkinnyJapaneseAnalyzer.default
    analyzer.toKatanaka("東京特許許可局") should equal("トウキョウトッキョキョカキョク")
    analyzer.toHiragana("東京特許許可局") should equal("とうきょうとっきょきょかきょく")
    analyzer.toRomaji("東京特許許可局") should equal("tokyotokkyokyokakyoku")
  }

}
