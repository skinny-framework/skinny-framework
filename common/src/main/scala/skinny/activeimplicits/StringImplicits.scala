package skinny.activeimplicits

import java.util.Locale

import org.joda.time._
import skinny.nlp.{ SkinnyJapaneseAnalyzer, SkinnyJapaneseAnalyzerFactory }
import skinny.util.{ DateTimeUtil, StringUtil }

import scala.language.implicitConversions

/**
  * ActiveSupport-ish implicit conversions to String value.
  */
object StringImplicits extends StringImplicits

trait StringImplicits {

  /**
    * http://api.rubyonrails.org/classes/String.html
    */
  case class RichString(str: String) {
    private[this] def withString(op: (String) => String): String = Option(str).map(op).orNull[String]
    private[this] def toBeginIndex(position: Int): Int =
      Option(str)
        .map { s =>
          if (position < 0) s.size - position.abs else position
        }
        .getOrElse(position)

    // acts_like_string?()
    // won't be supported because this is a Ruby specific feature

    // at(position)

    def at(position: Int): String = withString { s =>
      val pos = toBeginIndex(position)
      if (pos + 1 > s.size) null
      else s.substring(pos, pos + 1)
    }

    def at(range: Range): String = withString { s =>
      range.headOption
        .map { head =>
          val begin = toBeginIndex(head)
          val end   = if (head < 0) str.size - range.last.abs + 1 else range.last + 1

          if (end > s.size) "" else s.substring(begin, end)
        }
        .getOrElse("")
    }

    // blank?()

    def blank: Boolean = Option(str).map(_.replaceAll("\\s+", "").length == 0).getOrElse(true)

    // camelcase(first_letter = :upper)

    def camelcase      = camelize
    def lowerCamelcase = lowerCamelize

    def camelcaseAsRuby      = camelizeAsRuby
    def lowerCamelcaseAsRuby = lowerCamelizeAsRuby

    def camelcaseAsScala      = camelizeAsScala
    def lowerCamelcaseAsScala = lowerCamelizeAsScala

    // camelize(first_letter = :upper)

    def camelize      = camelizeAsScala
    def lowerCamelize = lowerCamelizeAsScala

    def camelizeAsRuby: String =
      StringUtil
        .toCamelCase(str)
        .split("/")
        .map { s =>
          s.head.toUpper + s.tail
        }
        .mkString("::")
    def lowerCamelizeAsRuby: String = {
      val s = camelizeAsRuby
      s.head.toLower + s.tail
    }

    def camelizeAsScala: String = withString { s =>
      val elements = s.split("/")
      elements.size match {
        case 0 => null
        case 1 => StringUtil.toUpperCamelCase(elements.head)
        case _ => (elements.init :+ StringUtil.toUpperCamelCase(elements.last)).mkString(".")
      }
    }
    def lowerCamelizeAsScala: String = withString { s =>
      val elements = s.split("/")
      elements.size match {
        case 0 => null
        case 1 => StringUtil.toCamelCase(elements.head)
        case _ => (elements.init :+ StringUtil.toCamelCase(elements.last)).mkString(".")
      }
    }

    // classify()
    // TODO: inflection required

    // constantize()
    // won't be supported because this is a Ruby specific feature

    // dasherize()

    def dasherize: String = withString(_.replaceAll("_", "-"))

    // deconstantize()

    // demodulize()
    // won't be supported because this is a Ruby specific feature

    // exclude?(string)

    def include(part: String): Boolean = Option(str).map(_.indexOf(part) != -1).getOrElse(false)
    def exclude(part: String): Boolean = !include(part)

    // first(limit = 1)

    def first: String              = withString(_.head.toString)
    def first(length: Int): String = withString(_.take(length))

    // foreign_key(separate_class_name_and_id_with_underscore = true)

    // from(position)
    // to(position)

    def from(position: Int): String        = withString(_.drop(toBeginIndex(position)))
    def to(position: Int): String          = withString(_.take(toBeginIndex(position) + 1))
    def fromTo(from: Int, to: Int): String = withString(_.to(to).from(from))

    // html_safe()
    // humanize(options = {})
    // TODO: inflection required

    // in_time_zone(zone = ::Time.zone)

    // indent(amount, indent_string=nil, indent_empty_lines=false)
    // indent!(amount, indent_string=nil, indent_empty_lines=false)

    // inquiry()

    // is_utf8?()

    // last(limit = 1)

    // mb_chars()

    // parameterize(sep = '-')

    def parameterize: String = parameterize("-")

    def parameterize(sep: String): String = {
      withString(
        _.replaceAll("\\s+", sep)
          .replaceAll("\\.", "")
          .toLowerCase(Locale.ENGLISH)
      )
    }

    // pluralize(count = nil, locale = :en)
    // TODO: inflection required

    // remove(*patterns)
    // remove!(*patterns)

    def remove(regexp: String): String = withString(_.replaceAll(regexp, ""))

    // safe_constantize()

    // singularize(locale = :en)
    // TODO: inflection required

    // squish()
    // squish!()

    def squish: String = withString(_.replaceAll("\\s+", " ").trim)

    // strip_heredoc()

    // tableize()
    // TODO: inflection required

    // titlecase()

    def titlecase = titleize

    // titleize()

    def titleize: String = withString { s =>
      s.replaceAll("-", " ")
        .split("\\s+")
        .map { s =>
          s.head.toUpper + s.tail
        }
        .mkString(" ")
    }

    // to_date()

    def toJodaLocalDate: LocalDate = DateTimeUtil.parseLocalDate(str)

    // to_datetime()

    def toJodaDateTime: DateTime = DateTimeUtil.parseDateTime(str)

    // to_time(form = :local)

    def toJodaLocalTime: LocalTime = DateTimeUtil.parseLocalTime(str)

    // truncate(truncate_at, options = {})

    def truncate(at: Int, separator: String = "", omission: String = "..."): String = {
      val o   = Option(omission).getOrElse("")
      val sep = Option(separator).getOrElse("")
      withString { s =>
        val res = s.take(at - o.size)
        if (sep.isEmpty) res + o
        else res.replaceFirst(sep + res.split(sep).last + "$", "") + o
      }
    }

    // truncate_words(words_count, options = {})

    def truncateWords(count: Int, separator: String = "\\s+", omission: String = "..."): String = {
      val o   = Option(omission).getOrElse("")
      val sep = Option(separator).getOrElse("")
      withString { s =>
        val regexpToRemove = s.split(sep).drop(count).foldLeft("") {
          case (res, word) => res + sep + word
        }
        s.replaceFirst(regexpToRemove + "$", "") + o
      }
    }

    // underscore()

    def underscore: String = RichString(underscoreAsRuby).underscoreAsScala

    def underscoreAsRuby: String = {
      Option(StringUtil.toSnakeCase(str))
        .map(_.split("::").map(_.replaceFirst("^_", "")).mkString("/"))
        .orNull[String]
    }

    def underscoreAsScala: String = {
      Option(StringUtil.toSnakeCase(str))
        .map(_.split("\\.").map(_.replaceFirst("^_", "")).mkString("/"))
        .orNull[String]
    }

    // ----------------------------
    // Japanese word conversions
    // requires kuromoji analyzer - http://lucene.apache.org/

    def katakanaReadings(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default): Seq[String] = {
      Option(str)
        .map { s =>
          SkinnyJapaneseAnalyzerFactory.ensureKuromojiExistence
          analyzer.toKatakanaReadings(s)
        }
        .getOrElse(Nil)
    }

    def hiraganaReadings(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default): Seq[String] = {
      Option(str)
        .map { s =>
          SkinnyJapaneseAnalyzerFactory.ensureKuromojiExistence
          analyzer.toHiraganaReadings(s)
        }
        .getOrElse(Nil)
    }

    def romajiReadings(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default): Seq[String] = {
      Option(str)
        .map { s =>
          SkinnyJapaneseAnalyzerFactory.ensureKuromojiExistence
          analyzer.toRomajiReadings(s)
        }
        .getOrElse(Nil)

    }

    def toKatakanaReadings(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default) =
      katakanaReadings
    def toHiraganaReadings(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default) =
      hiraganaReadings
    def toRomajiReadings(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default) = romajiReadings

    def katakana(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default): String = withString { s =>
      this.katakanaReadings.mkString
    }

    def hiragana(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default): String = withString { s =>
      this.hiraganaReadings.mkString
    }

    def romaji(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default): String = withString { s =>
      this.romajiReadings.mkString
    }

    def toKatakana(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default) = katakana
    def toHiragana(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default) = hiragana
    def toRomaji(implicit analyzer: SkinnyJapaneseAnalyzer = SkinnyJapaneseAnalyzer.default)   = romaji

    // ----------------------------

  }

  implicit def fromStringToRichString(str: String): RichString = RichString(str)

}
