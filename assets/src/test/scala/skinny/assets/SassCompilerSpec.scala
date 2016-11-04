package skinny.assets

import org.scalatest._
import org.slf4j.LoggerFactory
import skinny.ScalaVersion

class SassCompilerSpec extends FlatSpec with Matchers {

  val logger = LoggerFactory.getLogger(classOf[SassCompilerSpec])

  behavior of "SassCompiler"

  if (ScalaVersion.is_2_12) {

    it should "skipp SassCompiler testing for Scala 2.12" in {
    }

  } else {

    it should "compile scss code" in {
      val compiler = SassCompiler
      val css = compiler.compile(
        "font.scss",
        """$font-stack: Helvetica, sans-serif;
          |
          |body {
          |  font: 100% $font-stack;
          |}
        """.stripMargin
      )

      css.replaceFirst("\n$", "") should equal(
        """body {
          |  font: 100% Helvetica, sans-serif; }""".stripMargin
      )
    }

    it should "compile indented-sass code" in {
      val compiler = SassCompiler
      val css = compiler.compileIndented(
        "main.sass",
        """#main
          |  color: blue
          |  font-size: 0.3em
        """.stripMargin
      )

      css.replaceFirst("\n$", "") should equal(
        """#main {
          |  color: blue;
          |  font-size: 0.3em; }""".stripMargin
      )
    }
  }

}
