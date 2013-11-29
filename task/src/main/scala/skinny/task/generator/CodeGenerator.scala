package skinny.task.generator

import java.io.File
import org.apache.commons.io.FileUtils

/**
 * Code generator.
 */
trait CodeGenerator {

  protected def toVariable(name: String) = name.head.toLower + name.tail

  protected def toClassName(name: String) = name.head.toUpper + name.tail

  protected def isOptionClassName(t: String): Boolean = t.trim().startsWith("Option")

  protected def toParamType(t: String): String = t.replaceFirst("Option\\[", "").replaceFirst("\\]", "").trim()

  protected def addDefaultValueIfOption(t: String): String = {
    if (t.startsWith("Option")) s"${t.trim()} = None" else t.trim()
  }

  protected def toExtractorMethodName(t: String): String = {
    val method = toParamType(t).head.toLower + toParamType(t).tail
    if (t.startsWith("Option")) method + "Opt"
    else method
  }

  protected def forceWrite(file: File, code: String) {
    FileUtils.forceMkdir(file.getParentFile)
    if (file.exists()) {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" modified.")
    } else {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" created.")
    }
  }

  protected def writeIfAbsent(file: File, code: String) {
    FileUtils.forceMkdir(file.getParentFile)
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" created.")
    }
  }

  protected def writeAppending(file: File, code: String) {
    FileUtils.forceMkdir(file.getParentFile)
    if (file.exists()) {
      FileUtils.write(file, code, true)
      println("  \"" + file.getPath + "\" modified.")
    } else {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" created.")
    }
  }

  protected def showSkinnyGenerator(): Unit = {
    println("""
 *** Skinny Generator Task ***
""")
  }

}
