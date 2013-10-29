package skinny.task

/**
 * Code generator.
 */
trait CodeGenerator {

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

}
