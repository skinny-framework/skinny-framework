package skinny.assets

import org.scalatest._

class SassCompilerSpec extends FlatSpec with Matchers {

  behavior of "SassCompiler"

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
