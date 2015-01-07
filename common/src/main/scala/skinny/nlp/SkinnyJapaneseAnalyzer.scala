package skinny.nlp

object SkinnyJapaneseAnalyzer {

  lazy val default: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzerFactory.create()
}

trait SkinnyJapaneseAnalyzer {

  def toKatakanaReadings(str: String): Seq[String]

  def toHiraganaReadings(str: String): Seq[String]

  def toRomajiReadings(str: String): Seq[String]

  def toRomaji(str: String): String

  def toHiragana(str: String): String

  def toKatanaka(str: String): String

}
