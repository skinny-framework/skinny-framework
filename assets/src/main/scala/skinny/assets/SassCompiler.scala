package skinny.assets

import scala.sys.process._
import java.io.{ IOException, ByteArrayInputStream }
import skinny.util.LoanPattern._
import skinny.exception.AssetsPrecompileFailureException
import org.slf4j.LoggerFactory

/**
 * Sass Compiler
 *
 * @see https://github.com/jlitola/play-sass
 */
class SassCompiler {

  private[this] val log = LoggerFactory.getLogger(classOf[SassCompiler])

  private[this] def isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0

  private[this] def sassCommand = if (isWindows) "sass.bat" else "sass"

  /**
   * Ensures sass command exists.
   */
  private[this] def ensureSassCommand() = {
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
    val (out, err) = (new StringBuilder, new StringBuilder)
    val processLogger = ProcessLogger(
      (o: String) => out ++= o ++= "\n",
      (e: String) => err ++= e ++= "\n"
    )
    using(new ByteArrayInputStream(scssCode.getBytes)) { stdin =>
      val exitCode = (Seq(sassCommand, "--stdin", "--trace", "--scss") #< stdin) ! processLogger
      if (exitCode == 0) out.toString
      else {
        val message = s"Failed to compile scss code! (exit code: ${exitCode})\n\n${err.toString}"
        log.error(message)
        throw new AssetsPrecompileFailureException(message)
      }
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
    val (out, err) = (new StringBuilder, new StringBuilder)
    val processLogger = ProcessLogger(
      (o: String) => out ++= o ++= "\n",
      (e: String) => err ++= e ++= "\n"
    )
    using(new ByteArrayInputStream(sassCode.getBytes)) { stdin =>
      val exitCode = (Seq(sassCommand, "--stdin", "--trace") #< stdin) ! processLogger
      if (exitCode == 0) out.toString
      else {
        val message = s"Failed to compile sass code! (exit code: ${exitCode})\n\n${err.toString}"
        log.error(message)
        throw new AssetsPrecompileFailureException(message)
      }
    }
  }

}

object SassCompiler extends SassCompiler