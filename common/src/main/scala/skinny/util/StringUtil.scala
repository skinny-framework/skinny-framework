package skinny.util

import scala.collection.mutable.ListBuffer
import java.util.Locale.ENGLISH

/**
  * String utility.
  */
object StringUtil {

  private[this] val acronymRegExpStr         = "[A-Z]{2,}"
  private[this] val acronymRegExp            = acronymRegExpStr.r
  private[this] val endsWithAcronymRegExpStr = "[A-Z]{2,}$"
  private[this] val singleUpperCaseRegExp    = """[A-Z]""".r

  /**
    * Converts String value to snake_case'd value.
    *
    * @param str string value
    * @return snake_case'd value
    */
  def toSnakeCase(str: String): String = {
    Option(str)
      .map { s =>
        // first, applies acronyms filter
        val acronymsFiltered = acronymRegExp.replaceAllIn(
          acronymRegExp
            .findFirstMatchIn(s)
            .map { m =>
              s.replaceFirst(endsWithAcronymRegExpStr, "_" + m.matched.toLowerCase(ENGLISH))
            }
            .getOrElse(s), // might end with an acronym
          { m =>
            "_" + m.matched.init.toLowerCase(ENGLISH) + "_" + m.matched.last.toString.toLowerCase(ENGLISH)
          }
        )
        // second, convert single upper case char to '_' + c.toLower
        val result = singleUpperCaseRegExp
          .replaceAllIn(acronymsFiltered, { m =>
            "_" + m.matched.toLowerCase(ENGLISH)
          })
          .replaceFirst("^_", "")
          .replaceFirst("_$", "")

        if (str.startsWith("_")) "_" + result
        else if (str.endsWith("_")) result + "_"
        else result
      }
      .orNull[String]
  }

  /**
    * Converts String value to camelCase value.
    *
    * @param str string value
    * @return camelCase value
    */
  def toCamelCase(str: String): String = {
    Option(str)
      .map { s =>
        val result = toUpperCamelCase(s)
        if (result.headOption.exists(c => c.isUpper)) s"${result.head.toLower}${result.tail}"
        else result
      }
      .orNull[String]
  }

  def toUpperCamelCase(str: String): String = {
    Option(str)
      .map { s =>
        val result = s
          .foldLeft((ListBuffer[Char](), false)) {
            case ((cs, prevIs_), c) =>
              if (c == '_') (cs, true)
              else if (prevIs_) (cs += c.toUpper, false)
              else (cs += c, false)
          }
          ._1
          .mkString
        if (result.headOption.exists(c => c.isLower)) s"${result.head.toUpper}${result.tail}"
        else result
      }
      .orNull[String]
  }

}
