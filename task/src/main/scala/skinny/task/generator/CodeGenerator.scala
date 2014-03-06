package skinny.task.generator

import java.io.File
import org.apache.commons.io.FileUtils
import skinny.util.StringUtil

/**
 * Code generator.
 */
trait CodeGenerator {

  protected def toVariable(name: String) = name.head.toLower + name.tail

  protected def toClassName(name: String) = name.head.toUpper + name.tail

  protected def toNamespace(basePackage: String, namespaces: Seq[String]): String =
    (Seq(basePackage) ++ namespaces).filter(!_.isEmpty).reduceLeft { (a, b) => a + "." + b }

  protected def toDirectoryPath(baseDir: String, namespaces: Seq[String]): String =
    (Seq(baseDir) ++ namespaces).filter(!_.isEmpty).reduceLeft { (a, b) => a + "/" + b }

  protected def toResourcesBasePath(namespaces: Seq[String]): String = if (namespaces.filter(!_.isEmpty).isEmpty) ""
  else "/" + namespaces.filter(!_.isEmpty).reduceLeft { (a, b) => a + "/" + b }

  protected def toControllerClassName(name: String) = toClassName(name) + "Controller"

  protected def isOptionClassName(t: String): Boolean = t.trim().startsWith("Option")

  protected def toParamType(t: String): String = t.replaceFirst("Option\\[", "").replaceFirst("\\]", "").trim()

  protected def toCamelCase(v: String): String = StringUtil.toCamelCase(v)

  protected def toSnakeCase(v: String): String = StringUtil.toSnakeCase(v)

  protected def toSplitName(v: String): String = toSnakeCase(v).split("_").toSeq.mkString(" ")

  protected def toFirstCharLower(s: String): String = s.head.toLower + s.tail

  protected def toCapitalizedSplitName(v: String): String = {
    toSnakeCase(v).split("_").toSeq
      .map(word => word.head.toUpper + word.tail)
      .mkString(" ")
  }

  protected def addDefaultValueIfOption(t: String): String = {
    if (t.startsWith("Option")) s"${t.trim()} = None" else t.trim()
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

  protected def showErrors(messages: Seq[String]) = {
    showSkinnyGenerator()
    println("""  Command failed!""")
    println("")
    println(messages.mkString("  Error: ", "\n", "\n"))
  }

}
