package skinny.nlp

import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.analysis.ja.tokenattributes.ReadingAttribute
import org.apache.lucene.analysis.ja.util.ToStringUtil
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import skinny.logging.LoggerProvider
import skinny.util.LoanPattern._

import scala.collection.mutable.ListBuffer
import scala.util.Try

case class KuromojiJapaneseAnalyzer(kuromojiAnalyzer: JapaneseAnalyzer) extends SkinnyJapaneseAnalyzer with LoggerProvider {

  private[this] val KATAKANA_CHARS_TO_BE_AS_IS = Seq('゠', '・', 'ー', 'ヽ', 'ヾ', 'ヿ')

  def toKatakanaReadings(str: String): Seq[String] = toTokens(str).map(_.katakana).toSeq

  def toHiraganaReadings(str: String): Seq[String] = {
    toKatakanaReadings(str).map(katakanaToHiragana)
  }

  def toRomajiReadings(str: String): Seq[String] = {
    toTokens(str).map(_.romaji).map(_.replaceAll("ō", "o")).toSeq
  }

  def toRomaji(str: String): String = toRomajiReadings(str).mkString

  def toHiragana(str: String): String = katakanaToHiragana(toKatanaka(str))

  def toKatanaka(str: String): String = toKatakanaReadings(str).mkString

  private def isKatakanaToBeHiragana(c: Char): Boolean = {
    Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA &&
      !KATAKANA_CHARS_TO_BE_AS_IS.contains(c)
  }

  private def katakanaToHiragana(str: String): String = {
    str.map { c => if (isKatakanaToBeHiragana(c)) (c + 'あ' - 'ア').toChar else c }.mkString
  }

  private case class KuromojiToken(term: String, katakana: String, romaji: String)

  private def toTokens(str: String): Seq[KuromojiToken] = {
    using(kuromojiAnalyzer.tokenStream("katakana-conversion", str)) { stream =>
      val charTermAttr = stream.addAttribute(classOf[CharTermAttribute])
      val readingAttr = stream.addAttribute(classOf[ReadingAttribute])

      val tokens = new ListBuffer[KuromojiToken]
      stream.reset()
      while (Try(stream.incrementToken()).getOrElse(true)) {
        val original = charTermAttr.toString
        if (original != null) {
          val katakana = if (readingAttr.getReading != null) readingAttr.getReading else original
          val romaji = ToStringUtil.getRomanization(katakana)
          val token = KuromojiToken(original, katakana, romaji)
          tokens.append(token)
        }
      }
      logger.debug(s"Tokenized results: ${tokens}")

      var previous: KuromojiToken = null
      val distinctTokens = new ListBuffer[KuromojiToken]
      tokens.foreach { current =>
        if (previous != null) {
          if (current.term.contains(previous.term)) {
            distinctTokens.remove(distinctTokens.size - 1)
            distinctTokens.append(current)
          } else if (previous.term.contains(current.term)) {
            // NOOP
          } else {
            distinctTokens.append(current)
          }
        } else {
          distinctTokens.append(current)
        }
        previous = current
      }
      distinctTokens.toSeq
    }
  }

}
