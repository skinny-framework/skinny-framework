package skinny.assets


import org.mozilla.javascript._
import java.io.InputStreamReader
import skinny.assets.LoanPattern._

/**
 * CoffeeScript compiler.
 *
 * @see https://github.com/Filirom1/concoct
 */
case class CoffeeScriptCompiler(bare: Boolean = false) {

  private[this] lazy val globalScope: ScriptableObject = {
    val context = Context.enter
    context.setOptimizationLevel(-1)
    val globalScope = context.initStandardObjects

    ClassPathResourceLoader.getResourceAsStream("META-INF/skinny-assets/coffee-script.js").map { coffee =>
      using(new InputStreamReader(coffee)) { input =>
        context.evaluateReader(globalScope, input, "coffeeScript", 0, null)
      }
    }

    globalScope
  }

  /**
   * Compiles CoffeeScript source code to JavaScript source code.
   *
   * @param coffeeScriptCode coffee code
   * @return js code
   */
  def compile(coffeeScriptCode: String): String = {
    val context = Context.enter
    val compileScope = context.newObject(globalScope)
    compileScope.setParentScope(globalScope)
    compileScope.put("coffeeScriptSource", compileScope, coffeeScriptCode)
    val compilerScript = s"CoffeeScript.compile(coffeeScriptSource, {bare: ${bare}});"
    context.evaluateString(compileScope, compilerScript, "skinny.assets.CoffeeScriptCompiler", 0, null).toString
  }

}

