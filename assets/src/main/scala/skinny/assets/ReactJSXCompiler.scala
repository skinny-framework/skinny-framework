package skinny.assets

import org.mozilla.javascript._
import java.io.{ ByteArrayInputStream, IOException, InputStreamReader }
import skinny.util.LoanPattern._
import skinny.ClassPathResourceLoader

import scala.sys.process._
import org.slf4j.LoggerFactory
import skinny.exception.AssetsPrecompileFailureException
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.Context
import org.mozilla.javascript.JavaScriptException

/**
 * React JSX template compiler.
 *
 * @see https://github.com/facebook/react-rails
 */
class ReactJSXCompiler {

  private[this] val log = LoggerFactory.getLogger(classOf[ReactJSXCompiler])

  private[this] lazy val globalScope: ScriptableObject = {
    val context = Context.enter
    context.setOptimizationLevel(-1)
    val globalScope = context.initStandardObjects

    context.evaluateString(globalScope, "var global = global || this;", "jsx", 0, null)
    ClassPathResourceLoader.getClassPathResource("META-INF/skinny-assets/JSXTransformer.js").map { jsx =>
      using(jsx.stream) { stream =>
        using(new InputStreamReader(stream)) { input =>
          context.evaluateReader(globalScope, input, "jsx", 0, null)
        }
      }
    }

    globalScope
  }

  private[this] def isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0

  private[this] def jsxCommand = if (isWindows) "jsx.bat" else "jsx"

  private[this] def nativeCompilerDescription = Seq(jsxCommand, "--version").lines.mkString

  private[this] def nativeCompilerCommand = Seq(jsxCommand)

  private[this] def nativeCompilerExists: Boolean = {
    try !Seq(jsxCommand, "--version").lines.mkString.isEmpty
    catch { case e: IOException => false }
  }

  /**
   * Compiles React JSX Transformer source code to JavaScript source code.
   *
   * @param jsxCode jsx code
   * @return js code
   */
  def compile(jsxCode: String): String = {
    if (nativeCompilerExists) {
      // Native compiler (npm install -g react-tools)
      log.debug(s"Native React compiler is found. (version: ${nativeCompilerDescription})")

      val (out, err) = (new StringBuilder, new StringBuilder)
      val processLogger = ProcessLogger(
        (o: String) => out ++= o ++= "\n",
        (e: String) => err ++= e ++= "\n"
      )
      using(new ByteArrayInputStream(jsxCode.getBytes)) { stdin =>
        val exitCode = (nativeCompilerCommand #< stdin) ! processLogger
        if (exitCode == 0) out.toString
        else {
          val message = s"Failed to compile React jsx script code! (exit code: ${exitCode})\n\n${err.toString}"
          log.error(message)
          throw new AssetsPrecompileFailureException(message)
        }
      }
    } else {
      // Compiler on the Rhino JS engine
      val context = Context.enter
      val compileScope = context.newObject(globalScope)
      compileScope.setParentScope(globalScope)
      compileScope.put("source", compileScope, jsxCode)
      val compilerScript = s"global.JSXTransformer.transform(source).code;"

      try context.evaluateString(compileScope, compilerScript, "skinny.assets.ReactCompiler", 0, null).toString
      catch {
        case e: JavaScriptException =>
          val message = s"Failed to compile jsx template code! (${e.getMessage})"
          log.error(message)
          throw new AssetsPrecompileFailureException(message)
      }
    }
  }

}

