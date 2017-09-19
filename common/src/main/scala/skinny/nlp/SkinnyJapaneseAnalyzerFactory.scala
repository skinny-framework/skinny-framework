package skinny.nlp

import java.io.{ ByteArrayInputStream, InputStreamReader }
import org.apache.lucene.analysis.ja.{ JapaneseAnalyzer, JapaneseTokenizer }
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.CharArraySet
import scala.collection.JavaConverters._
import skinny.util.LoanPattern._

object SkinnyJapaneseAnalyzerFactory {

  def ensureKuromojiExistence: Unit = {
    if (kuromojiNotFound) {
      throw new IllegalStateException(
        """Kuromoji Analyzer Required
          |
          |-------------------------------------------
          |
          | ***** Kuromoji Analyzer Required *****
          |
          | Japanese converters (katakana, hiragana, romaji) requires Kuromoji analyzer from Apache Lucene. http://lucene.apache.org/
          |
          | Add the following dependency to your build.sbt.
          |
          |   libraryDependencies += "org.apache.lucene" % "lucene-analyzers-kuromoji" % "<luceneLatestVersion>"
          |
          |-------------------------------------------
          |""".stripMargin
      )
    }
  }

  lazy val kuromojiNotFound: Boolean = {
    try {
      Class.forName("org.apache.lucene.analysis.ja.JapaneseAnalyzer")
      false
    } catch { case scala.util.control.NonFatal(_) => true }
  }

  def create(dictionaryText: String): SkinnyJapaneseAnalyzer = {
    val dictionary: UserDictionary = using(new ByteArrayInputStream(dictionaryText.getBytes)) { stream =>
      using(new InputStreamReader(stream)) { reader =>
        UserDictionary.open(reader)
      }
    }
    create(dictionary)
  }

  def create(userDictionary: UserDictionary = null): SkinnyJapaneseAnalyzer = {
    ensureKuromojiExistence
    new KuromojiJapaneseAnalyzer(
      new JapaneseAnalyzer(
        userDictionary,
        JapaneseTokenizer.Mode.NORMAL,
        new CharArraySet(0, true),
        Set.empty[String].asJava
      )
    )
  }

}
