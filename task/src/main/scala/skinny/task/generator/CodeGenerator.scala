package skinny.task.generator

import java.io.File
import java.sql.Types._
import java.util.Locale
import org.apache.commons.io.FileUtils
import scalikejdbc.DB
import scalikejdbc.metadata.Column
import skinny.ParamType
import skinny.ParamType._
import skinny.util.StringUtil

import scala.io.Source

/**
 * Code generator.
 */
trait CodeGenerator {

  def toVariable(name: String) = name.head.toLower + name.tail

  def toClassName(name: String) = name.head.toUpper + name.tail

  def toNamespace(basePackage: String, namespaces: Seq[String]): String =
    (Seq(basePackage) ++ namespaces).filter(!_.isEmpty).reduceLeft { (a, b) => a + "." + b }

  def toDirectoryPath(baseDir: String, namespaces: Seq[String]): String = {
    val dirs: Seq[String] = (Seq(baseDir) ++ namespaces).filter(!_.isEmpty)
    if (dirs.isEmpty) "" else dirs.reduceLeft { (a, b) => a + "/" + b }
  }

  def toResourcesBasePath(namespaces: Seq[String]): String = if (namespaces.filter(!_.isEmpty).isEmpty) ""
  else "/" + namespaces.filter(!_.isEmpty).reduceLeft { (a, b) => a + "/" + b }

  def toControllerClassName(name: String) = toClassName(name) + "Controller"

  def isOptionClassName(t: String): Boolean = t.trim().startsWith("Option")

  def toParamType(t: String): String = t.replaceFirst("Option\\[", "").replaceFirst("\\]", "").trim()

  def toCamelCase(v: String): String = StringUtil.toCamelCase(v)

  def toSnakeCase(v: String): String = StringUtil.toSnakeCase(v)

  def toSplitName(v: String): String = toSnakeCase(v).split("_").toSeq.mkString(" ")

  def toFirstCharLower(s: String): String = s.head.toLower + s.tail

  def toCapitalizedSplitName(v: String): String = {
    toSnakeCase(v).split("_").toSeq
      .map(word => word.head.toUpper + word.tail)
      .mkString(" ")
  }

  def toScalaTypeName(paramTypeName: String): String = paramTypeName match {
    case "ByteArray" => "Array[Byte]"
    case "Option[ByteArray]" => "Option[Array[Byte]]"
    case _ => paramTypeName
  }

  def toScalaTypeNameWithDefaultValueIfOption(paramTypeName: String): String = {
    val scalaTypeName = toScalaTypeName(paramTypeName.trim())
    if (scalaTypeName.startsWith("Option")) s"${scalaTypeName} = None"
    else scalaTypeName
  }

  def forceWrite(file: File, code: String) {
    FileUtils.forceMkdir(file.getParentFile)
    if (file.exists()) {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" modified.")
    } else {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def writeIfAbsent(file: File, code: String) {
    FileUtils.forceMkdir(file.getParentFile)
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def writeAppending(file: File, code: String) {
    FileUtils.forceMkdir(file.getParentFile)
    if (file.exists()) {
      FileUtils.write(file, code, true)
      println("  \"" + file.getPath + "\" modified.")
    } else {
      FileUtils.write(file, code)
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def showSkinnyGenerator(): Unit = {
    println("""
 *** Skinny Generator Task ***
""")
  }

  def showErrors(messages: Seq[String]) = {
    showSkinnyGenerator()
    println("""  Command failed!""")
    println("")
    println(messages.mkString("  Error: ", "\n", "\n"))
  }

  def toControllerName(namespaces: Seq[String], resources: String): String = {
    if (namespaces.filterNot(_.isEmpty).isEmpty) toCamelCase(resources)
    else namespaces.head + namespaces.tail.map { n => n.head.toUpper + n.tail }.mkString + toClassName(resources)
  }

  def appendToControllers(namespaces: Seq[String], name: String) {
    val controllerName = toControllerName(namespaces, name)
    val controllerClassName = toNamespace("_root_.controller", namespaces) + "." + toControllerClassName(name)
    val newMountCode =
      s"""def mount(ctx: ServletContext): Unit = {
        |    ${controllerName}.mount(ctx)""".stripMargin
    val newControllerDefCode = {
      s"""  object ${controllerName} extends ${controllerClassName} with Routes {
        |  }
        |
        |}
        |""".stripMargin
    }

    val file = new File("src/main/scala/controller/Controllers.scala")
    if (file.exists()) {
      val code = Source.fromFile(file).mkString
        .replaceFirst("(def\\s+mount\\s*\\(ctx:\\s+ServletContext\\):\\s*Unit\\s*=\\s*\\{)", newMountCode)
        .replaceFirst("(}[\\s\\r\\n]+)$", newControllerDefCode)
      forceWrite(file, code)
    } else {
      val fullNewCode =
        s"""package controller
          |
          |import _root_.controller._
          |import skinny._
          |import skinny.controller.AssetsController
          |
          |object Controllers {
          |
          |  ${newMountCode}
          |    AssetsController.mount(ctx)
          |  }
          |
          |${newControllerDefCode}
          |""".stripMargin
      forceWrite(file, fullNewCode)
    }
  }

  def extractColumns(tableName: String): List[Column] = {
    DB.getTable(tableName).map { table =>
      table.columns
    }.getOrElse {
      throw new IllegalStateException(s"Failed to retrieve meta data about columns for ${tableName}")
    }
  }

  def convertJdbcSqlTypeToParamType(dataType: Int): ParamType = dataType match {
    case CHAR | VARCHAR | LONGVARCHAR | LONGNVARCHAR | NCHAR | NVARCHAR | CLOB | NCLOB => String
    case BOOLEAN | BIT => Boolean
    case TINYINT => Byte
    case SMALLINT => Short
    case INTEGER => Int
    case BIGINT => Long
    case FLOAT | REAL => Float
    case DOUBLE => Double
    case NUMERIC | DECIMAL => BigDecimal
    case DATE => LocalDate
    case TIME => LocalTime
    case TIMESTAMP => DateTime
    case BINARY | VARBINARY | LONGVARBINARY | BLOB => ByteArray
    case _ => String
  }

  def toScaffoldFieldDef(column: Column): String = {
    val paramType = toParamType(column)
    val paramTypeString: String = if (column.isRequired) paramType.toString else s"Option[$paramType]"
    val columnTypeString: String = {
      paramType match {
        case String => s":varchar(${column.size})"
        case BigDecimal => s":number(${column.size})"
        case _ => ""
      }
    }
    toCamelCase(column.name.toLowerCase(Locale.ENGLISH)) + ":" + paramTypeString + columnTypeString
  }

  def toParamType(column: Column): ParamType = convertJdbcSqlTypeToParamType(column.typeCode)

}
