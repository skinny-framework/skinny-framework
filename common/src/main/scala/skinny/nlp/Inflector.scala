/*
The MIT License (MIT)
Copyright (c) 2011 Mojolly Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software
and associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package skinny.nlp

import java.util.Locale._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/**
  * Based on github.com/backchatio/scala-inflector
  */
trait Inflector {

  import Inflector._

  private object Rule {
    def apply(kv: (String, String)) = new Rule(kv._1, kv._2)
  }

  private class Rule(pattern: String, replacement: String) {

    private[this] val regex = ("""(?i)%s""" format pattern).r

    def apply(word: String) = {
      if (regex.findFirstIn(word).isEmpty) {
        None
      } else {
        val m = regex.replaceAllIn(word, replacement)
        if (m == null || m.trim.isEmpty) None
        else Some(m)
      }
    }
  }

  private[this] val plurals: ListBuffer[Rule]        = new ListBuffer[Rule]()
  private[this] val singulars: ListBuffer[Rule]      = new ListBuffer[Rule]()
  private[this] val uncountables: ListBuffer[String] = new ListBuffer[String]()

  private[this] lazy val fixedPlurals: Seq[Rule]        = plurals.reverse.toIndexedSeq
  private[this] lazy val fixedSingulars: Seq[Rule]      = singulars.reverse.toIndexedSeq
  private[this] lazy val fixedUncountables: Seq[String] = uncountables.reverse.toIndexedSeq

  def addPlural(pattern: String, replacement: String): Unit = {
    plurals += Rule(pattern -> replacement)
  }

  def addSingular(pattern: String, replacement: String): Unit = {
    singulars += Rule(pattern -> replacement)
  }

  def addIrregular(singular: String, plural: String): Unit = {
    plurals += Rule(("(" + singular(0) + ")" + singular.substring(1) + "$") -> ("$1" + plural.substring(1)))
    singulars += Rule(("(" + plural(0) + ")" + plural.substring(1) + "$")   -> ("$1" + singular.substring(1)))
  }

  def addUncountable(word: String): Unit = {
    uncountables += word
  }

  def titleize(word: String): String = {
    """\b([a-z])""".r.replaceAllIn(
      humanize(underscore(word)),
      _.group(0).toUpperCase(ENGLISH)
    )
  }

  def humanize(word: String): String = capitalize(word.replace("_", " "))

  def camelize(word: String): String = {
    val w = pascalize(word)
    w.substring(0, 1).toLowerCase(ENGLISH) + w.substring(1)
  }

  def pascalize(word: String): String = {
    val lst = word.split("_").toList
    (lst.headOption.map(s ⇒ s.substring(0, 1).toUpperCase(ENGLISH) + s.substring(1)).get ::
    lst.tail.map(s ⇒ s.substring(0, 1).toUpperCase + s.substring(1))).mkString("")
  }

  def underscore(word: String): String = {
    val replacementPattern = "$1_$2"
    spacesPattern
      .replaceAllIn(
        secondPattern.replaceAllIn(
          firstPattern.replaceAllIn(
            word,
            replacementPattern
          ),
          replacementPattern
        ),
        "_"
      )
      .toLowerCase
  }

  def capitalize(word: String): String = {
    word.substring(0, 1).toUpperCase(ENGLISH) + word.substring(1).toLowerCase(ENGLISH)
  }

  def uncapitalize(word: String): String = {
    word.substring(0, 1).toLowerCase(ENGLISH) + word.substring(1)
  }

  def ordinalize(word: String): String = _ordanize(word.toInt, word)

  def ordinalize(number: Int): String = _ordanize(number, number.toString)

  private[this] def _ordanize(number: Int, numberString: String) = {
    val nMod100 = number % 100
    if (nMod100 >= 11 && nMod100 <= 13) {
      numberString + "th"
    } else {
      (number % 10) match {
        case 1 ⇒ numberString + "st"
        case 2 ⇒ numberString + "nd"
        case 3 ⇒ numberString + "rd"
        case _ ⇒ numberString + "th"
      }
    }
  }

  def dasherize(word: String): String = underscore(word).replace('_', '-')

  def pluralize(word: String): String = applyRules(fixedPlurals, word)

  def singularize(word: String): String = applyRules(fixedSingulars, word)

  @tailrec
  private[this] def applyRules(collection: Seq[Rule], word: String): String = {
    if (fixedUncountables.contains(word.toLowerCase(ENGLISH))) {
      word
    } else {
      if (collection.isEmpty) {
        word
      } else {
        collection.head(word) match {
          case Some(m) => m
          case _       => applyRules(collection.tail, word)
        }
      }
    }
  }

  def interpolate(text: String, vars: Map[String, String]): String = {
    """\#\{([^}]+)\}""".r.replaceAllIn(text, (_: Regex.Match) match {
      case Regex.Groups(v) ⇒ vars.getOrElse(v, "")
    })
  }

}

object Inflector extends Inflector {

  private val spacesPattern = "[-\\s]".r
  private val firstPattern  = "([A-Z]+)([A-Z][a-z])".r
  private val secondPattern = "([a-z\\d])([A-Z])".r

  class InflectorString(word: String) {
    def titleize                        = Inflector.titleize(word)
    def humanize                        = Inflector.humanize(word)
    def camelize                        = Inflector.camelize(word)
    def pascalize                       = Inflector.pascalize(word)
    def underscore                      = Inflector.underscore(word)
    def dasherize                       = Inflector.dasherize(word)
    def uncapitalize                    = Inflector.uncapitalize(word)
    def ordinalize                      = Inflector.ordinalize(word)
    def pluralize                       = Inflector.pluralize(word)
    def singularize                     = Inflector.singularize(word)
    def fill(values: (String, String)*) = Inflector.interpolate(word, Map(values: _*))
  }

  class InflectorInt(number: Int) {
    def ordinalize = Inflector.ordinalize(number)
  }

  addPlural("$", "s")
  addPlural("s$", "s")
  addPlural("(ax|test)is$", "$1es")
  addPlural("(octop|vir|alumn|fung)us$", "$1i")
  addPlural("(alias|status)$", "$1es")
  addPlural("(bu)s$", "$1ses")
  addPlural("(buffal|tomat|volcan)o$", "$1oes")
  addPlural("([ti])um$", "$1a")
  addPlural("sis$", "ses")
  addPlural("(?:([^f])fe|([lr])f)$", "$1$2ves")
  addPlural("(hive)$", "$1s")
  addPlural("([^aeiouy]|qu)y$", "$1ies")
  addPlural("(x|ch|ss|sh)$", "$1es")
  addPlural("(matr|vert|ind)ix|ex$", "$1ices")
  addPlural("([m|l])ouse$", "$1ice")
  addPlural("^(ox)$", "$1en")
  addPlural("(quiz)$", "$1zes")

  addSingular("s$", "")
  addSingular("(n)ews$", "$1ews")
  addSingular("([ti])a$", "$1um")
  addSingular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis")
  addSingular("(^analy)ses$", "$1sis")
  addSingular("([^f])ves$", "$1fe")
  addSingular("(hive)s$", "$1")
  addSingular("(tive)s$", "$1")
  addSingular("([lr])ves$", "$1f")
  addSingular("([^aeiouy]|qu)ies$", "$1y")
  addSingular("(s)eries$", "$1eries")
  addSingular("(m)ovies$", "$1ovie")
  addSingular("(x|ch|ss|sh)es$", "$1")
  addSingular("([m|l])ice$", "$1ouse")
  addSingular("(bus)es$", "$1")
  addSingular("(o)es$", "$1")
  addSingular("(shoe)s$", "$1")
  addSingular("(cris|ax|test)es$", "$1is")
  addSingular("(octop|vir|alumn|fung)i$", "$1us")
  addSingular("(alias|status)es$", "$1")
  addSingular("^(ox)en", "$1")
  addSingular("(vert|ind)ices$", "$1ex")
  addSingular("(matr)ices$", "$1ix")
  addSingular("(quiz)zes$", "$1")

  addIrregular("person", "people")
  addIrregular("man", "men")
  addIrregular("child", "children")
  addIrregular("sex", "sexes")
  addIrregular("move", "moves")
  addIrregular("goose", "geese")
  addIrregular("alumna", "alumnae")

  addUncountable("equipment")
  addUncountable("information")
  addUncountable("rice")
  addUncountable("money")
  addUncountable("species")
  addUncountable("series")
  addUncountable("fish")
  addUncountable("sheep")
  addUncountable("deer")
  addUncountable("aircraft")

}
