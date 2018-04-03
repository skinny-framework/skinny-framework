package skinny.assets

import org.mozilla.javascript._
import java.io.{ ByteArrayInputStream, IOException, InputStreamReader }

import skinny.util.LoanPattern._
import skinny.{ ClassPathResourceLoader, ScalaVersion, SkinnyEnv }

import scala.sys.process._
import org.slf4j.LoggerFactory
import skinny.exception.AssetsPrecompileFailureException

/**
  * CoffeeScript compiler.
  *
  * @see https://github.com/Filirom1/concoct
  */
case class CoffeeScriptCompiler(bare: Boolean = false) {

  private[this] val log = LoggerFactory.getLogger(classOf[CoffeeScriptCompiler])

  private[this] lazy val globalScope: ScriptableObject = {
    val context = Context.enter
    context.setOptimizationLevel(-1)
    val globalScope = context.initStandardObjects

    ClassPathResourceLoader.getClassPathResource("META-INF/skinny-assets/coffee-script.js").map { coffee =>
      using(coffee.stream) { stream =>
        using(new InputStreamReader(stream)) { input =>
          context.evaluateReader(globalScope, input, "coffeeScript", 0, null)
        }
      }
    }

    globalScope
  }

  private[this] def isWindows = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0

  private[this] def coffeeCommand = if (isWindows) "coffee.bat" else "coffee"

  private[this] def nativeCompilerDescription = Seq(coffeeCommand, "-v").lineStream.mkString

  private[this] def nativeCompilerCommand =
    Seq(coffeeCommand, "--compile", "--stdio") ++ (if (bare) Seq("--bare") else Nil)

  def sourceMapsEnabled: Boolean = SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()

  private[this] def nativeCompilerExists: Boolean = {
    if (ScalaVersion.is_2_12) {
      // calling an external process here fails in 2.12
      // java.lang.IllegalMonitorStateException:
      //  at java.lang.Object.wait(Native Method)
      false
    } else {
      try Seq(coffeeCommand, "-v").lineStream.size > 0
      catch { case e: IOException => false }
    }
  }

  /**
    * Compiles CoffeeScript source code to JavaScript source code.
    *
    * @param coffeeScriptCode coffee code
    * @return js code
    */
  def compile(fullpath: String, coffeeScriptCode: String): String = {
    if (nativeCompilerExists) {
      // Native compiler (npm install -g coffee-script)
      log.debug(s"Native CoffeeScript compiler is found. (version: ${nativeCompilerDescription})")

      val (out, err) = (new StringBuilder, new StringBuilder)
      val processLogger = ProcessLogger(
        (o: String) => out ++= o ++= "\n",
        (e: String) => err ++= e ++= "\n"
      )
      using(new ByteArrayInputStream(coffeeScriptCode.getBytes)) { stdin =>
        val process: Process = (nativeCompilerCommand #< stdin).run(processLogger)
        process.wait(5000)
        val exitCode = process.exitValue()
        if (exitCode == 0) {
          // create Source Maps file on the same directory
          if (sourceMapsEnabled) {
            fullpath.split("WEB-INF/assets/coffee/") match {
              case Array(dir, path) =>
                sys.process
                  .Process(Seq(coffeeCommand, "--map", path), new java.io.File(dir + "WEB-INF/assets/coffee"))
                  .!
                out.toString + s"\n\n//# sourceMappingURL=${fullpath.split("/").last.replaceFirst(".coffee", ".map")}"
              case _ =>
                out.toString
            }
          } else {
            out.toString
          }
        } else {
          val message = s"Failed to compile coffee script code! (exit code: ${exitCode})\n\n${err.toString}"
          log.error(message)
          throw new AssetsPrecompileFailureException(message)
        }
      }
    } else {
      // Compiler on the Rhino JS engine
      val context      = Context.enter
      val compileScope = context.newObject(globalScope)
      compileScope.setParentScope(globalScope)
      compileScope.put("coffeeScriptSource", compileScope, coffeeScriptCode)
      val compilerScript = s"CoffeeScript.compile(coffeeScriptSource, {bare: ${bare}});"
      try {
        context.evaluateString(compileScope, compilerScript, "skinny.assets.CoffeeScriptCompiler", 0, null).toString
      } catch {
        case e: JavaScriptException =>
          val message = s"Failed to compile coffee script code! (${e.getMessage})"
          log.error(message)
          throw new AssetsPrecompileFailureException(message)
      }
    }
  }

}
