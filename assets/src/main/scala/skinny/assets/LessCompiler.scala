package skinny.assets

import org.mozilla.javascript._
import skinny.assets.LoanPattern._
import java.io.InputStreamReader
import org.mozilla.javascript.tools.shell.Global

/**
 * Less Compiler
 *
 * @see https://github.com/Filirom1/concoct
 */
class LessCompiler {

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
      ClassPathResourceLoader.getResourceAsStream(jsName).map { js =>
        using(new InputStreamReader(js)) { input =>
          context.evaluateReader(scope, input, jsName, 0, null)
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
  def compile(lessCode: String): String = {
    Context.call(null, stringCompiler, scope, scope, Array(lessCode)).toString
  }

}

object LessCompiler extends LessCompiler

