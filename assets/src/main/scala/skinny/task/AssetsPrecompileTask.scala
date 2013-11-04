package skinny.task

import java.io.File
import org.apache.commons.io.FileUtils
import scala.collection.JavaConverters._
import skinny.assets._
import skinny.exception._

/**
 * assets:precompile task runner.
 */
object AssetsPrecompileTask {

  private[this] val coffeeScriptCompiler = CoffeeScriptCompiler()
  private[this] val lessCompiler = LessCompiler

  /**
   * skinny.task.AssetsPrecompileTask.main(Array("basedir"))
   */
  def main(args: Array[String]) {
    val baseDir = args.headOption.getOrElse(".") + "/src/main/webapp/WEB-INF/assets"
    val outputDir = args.headOption.getOrElse(".") + "/src/main/webapp/assets"

    FileUtils.forceMkdir(new File(baseDir))
    FileUtils.forceMkdir(new File(baseDir + "/js"))
    FileUtils.forceMkdir(new File(baseDir + "/css"))
    FileUtils.forceMkdir(new File(baseDir + "/coffee"))
    FileUtils.forceMkdir(new File(baseDir + "/less"))

    FileUtils.forceMkdir(new File(outputDir))

    FileUtils.copyDirectory(new File(baseDir + "/js"), new File(outputDir + "/js"))
    FileUtils.copyDirectory(new File(baseDir + "/css"), new File(outputDir + "/css"))

    // CoffeeScript
    FileUtils.listFiles(new File(baseDir + "/coffee"), Array("coffee"), true).asScala.foreach { file =>
      val code = FileUtils.readFileToString(file)
      val compiledCode = coffeeScriptCompiler.compile(code)
      val output = new File(outputDir + "/js/" + file.getName.replaceFirst("\\.coffee$", ".js"))
      FileUtils.forceMkdir(output.getParentFile)
      if (output.exists()) {
        throw new AssetsPrecompileFailureException("[ERROR] assets:precompile task failed! Reason: \"" + output.getPath + "\" already exists.")
      } else {
        FileUtils.write(output, compiledCode)
      }
    }

    // LESS
    FileUtils.listFiles(new File(baseDir + "/less"), Array("less"), true).asScala.foreach { file =>
      val code = FileUtils.readFileToString(file)
      val compiledCode = lessCompiler.compile(code)
      val output = new File(outputDir + "/css/" + file.getName.replaceFirst("\\.less$", ".css"))
      FileUtils.forceMkdir(output.getParentFile)
      if (output.exists()) {
        throw new AssetsPrecompileFailureException("[ERROR] assets:precompile task failed! Reason: \"" + output.getPath + "\" already exists.")
      } else {
        FileUtils.write(output, compiledCode)
      }
    }
  }

}
