package skinny.assets

import scala.sys.process._
import java.io.{ IOException, ByteArrayInputStream }
import skinny.util.LoanPattern._
import skinny.exception.AssetsPrecompileFailureException

/**
 * Sass Compiler
 *
 * @see https://github.com/jlitola/play-sass
 */
class SassCompiler {

  private def isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0

  private def sassCommand = if (isWindows) "sass.bat" else "sass"

  /**
   * Ensures sass command exists.
   */
  private def ensureSassCommand() = {
    try {
      Seq(sassCommand, "-v").lines // > /dev/null
    } catch {
      case e: IOException =>
        throw new AssetsPrecompileFailureException(
          "Failed to run sass command! sass should be prepared in advance. " +
            "If you haven't install sass yet, just run `gem install sass` now.")
    }
  }

  /**
   * Compiles scss code to css code.
   * @param scssCode scss code
   * @return css code
   */
  def compile(scssCode: String): String = {
    ensureSassCommand()
    using(new ByteArrayInputStream(scssCode.getBytes)) { stdin =>
      (Seq(sassCommand, "--stdin", "--trace", "--scss") #< stdin).lines.mkString("\n")
    }
  }

  /**
   * Compiles sass code to css code.
   *
   * @see http://sass-lang.com/documentation/file.INDENTED_SYNTAX.html
   * @param sassCode sass code
   * @return css code
   */
  def compileIndented(sassCode: String): String = {
    ensureSassCommand()
    using(new ByteArrayInputStream(sassCode.getBytes)) { stdin =>
      (Seq(sassCommand, "--stdin", "--trace") #< stdin).lines.mkString("\n")
    }
  }

}

object SassCompiler extends SassCompiler