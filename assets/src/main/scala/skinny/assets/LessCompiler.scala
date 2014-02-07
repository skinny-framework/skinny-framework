package skinny.assets

import org.mozilla.javascript._
import skinny.util.LoanPattern._
import java.io.{ ByteArrayInputStream, IOException, InputStreamReader }
import org.mozilla.javascript.tools.shell.Global
import skinny.ClassPathResourceLoader
import skinny.exception.AssetsPrecompileFailureException
import org.slf4j.LoggerFactory
import scala.sys.process._

/**
 * Less Compiler
 *
 * @see https://github.com/Filirom1/concoct
 */
class LessCompiler {

  private[this] val log = LoggerFactory.getLogger(classOf[LessCompiler])

  private[this] def isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0

  private[this] def lessCommand = if (isWindows) "lessc.bat" else "lessc"

  private[this] def nativeCompilerDescription = Seq(lessCommand, "-v").lines.mkString

  private[this] def nativeCompilerCommand = Seq(lessCommand, "-") // after "-" read stdin

  private[this] def nativeCompilerExists: Boolean = {
    try Seq(lessCommand, "-v").lines.size > 0
    catch { case e: IOException => false }
  }

  private[this] lazy val scope: Scriptable = {
    val context = Context.enter
    context.setOptimizationLevel(9)
    val global = new Global
    global.init(context)
    val scope = context.initStandardObjects(global)

    Seq(
      "META-INF/skinny-assets/less/browser.js",
      "META-INF/skinny-assets/less/less.js",
      "META-INF/skinny-assets/less/engine.js"
    ).foreach { jsName =>
        ClassPathResourceLoader.getClassPathResource(jsName).map { js =>
          using(js.stream) { stream =>
            using(new InputStreamReader(stream)) { input =>
              context.evaluateReader(scope, input, jsName, 0, null)
            }
          }
        }
      }

    scope
  }

  private[this] lazy val stringCompiler: Callable = {
    scope.get("compileString", scope).asInstanceOf[Callable]
  }

  /**
   * Compiles less code to css code
   * @param lessCode less code
   * @return css code
   */
  def compile(path: String, lessCode: String): String = {
    if (nativeCompilerExists) {
      // Native compiler (npm install -g less)
      log.debug(s"Native LESS compiler is found. (version: ${nativeCompilerDescription})")

      val (out, err) = (new StringBuilder, new StringBuilder)
      val processLogger = ProcessLogger(
        (o: String) => out ++= o ++= "\n",
        (e: String) => err ++= e ++= "\n"
      )
      using(new ByteArrayInputStream(lessCode.getBytes)) { stdin =>
        val exitCode = (nativeCompilerCommand #< stdin) ! processLogger
        if (exitCode == 0) out.toString
        else {
          val message = s"Failed to compile less code! (exit code: ${exitCode})\n\n${err.toString}"
          log.error(message)
          throw new AssetsPrecompileFailureException(message)
        }
      }
    } else {
      // Compiler on the Rhino JS engine
      try Context.call(null, stringCompiler, scope, scope, Array(lessCode)).toString
      catch {
        case e: EcmaError =>
          val message = s"Failed to compile less code!"
          log.error(message)
          throw new AssetsPrecompileFailureException(message)
      }
    }
  }

}

object LessCompiler extends LessCompiler

