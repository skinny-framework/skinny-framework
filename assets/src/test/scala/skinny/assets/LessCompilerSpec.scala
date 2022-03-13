package skinny.assets

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LessCompilerSpec extends AnyFlatSpec with Matchers {

  behavior of "LessCompiler"

  it should "compile code" in {
    val compiler = LessCompiler
    val css = compiler.compile(
      "box.less",
      """@base: #f938ab;
        |
        |.box-shadow(@style, @c) when (iscolor(@c)) {
        |  box-shadow:         @style @c;
        |  -webkit-box-shadow: @style @c;
        |  -moz-box-shadow:    @style @c;
        |}
        |.box-shadow(@style, @alpha: 50%) when (isnumber(@alpha)) {
        |  .box-shadow(@style, rgba(0, 0, 0, @alpha));
        |}
        |.box {
        |  color: saturate(@base, 5%);
        |  border-color: lighten(@base, 30%);
        |  div { .box-shadow(0 0 5px, 30%) }
        |}
      """.stripMargin
    )

    css should equal(
      """.box {
        |  color: #fe33ac;
        |  border-color: #fdcdea;
        |}
        |.box div {
        |  box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
        |  -webkit-box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
        |  -moz-box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
        |}
        |""".stripMargin
    )
  }

}
