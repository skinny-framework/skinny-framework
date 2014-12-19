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

  // ------------------------
  // configuration

  def sourceDir = "src/main/scala"

  def testSourceDir = "src/test/scala"

  def resourceDir = "src/main/resources"

  def testResourceDir = "src/test/resources"

  def webInfDir = "src/main/webapp/WEB-INF"

  // If you prefer Play Framework's style, override this and specify "controllers"
  def controllerPackage: String = "controller"

  def controllerPackageDir: String = controllerPackage.split("\\.").mkString("/")

  // If you prefer Play Framework's style, override this and specify "models"
  def modelPackage: String = "model"

  def modelPackageDir: String = modelPackage.split("\\.").mkString("/")

  // ------------------------
  // generator methods

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

  def appendToControllers(namespaces: Seq[String], name: String) {
    val controllerName = toControllerName(namespaces, name)
    val controllerClassName = toNamespace(s"_root_.${controllerPackage}", namespaces) + "." + toControllerClassName(name)
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

    val file = new File(s"${sourceDir}/${controllerPackage}/Controllers.scala")
    if (file.exists()) {
      val code = Source.fromFile(file).mkString
        .replaceFirst("(def\\s+mount\\s*\\(ctx:\\s+ServletContext\\):\\s*Unit\\s*=\\s*\\{)", newMountCode)
        .replaceFirst("(}[\\s\\r\\n]+)$", newControllerDefCode)
      forceWrite(file, code)
    } else {
      val fullNewCode =
        s"""package ${controllerPackage}
          |
          |import _root_.${controllerPackage}._
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

  // ------------------------
  // helper methods

  def toVariable(name: String) = toCamelCase(name)

  def toClassName(name: String) = name.head.toUpper + toCamelCase(name.tail)

  def toNamespace(basePackage: String, namespaces: Seq[String]): String = {
    (Seq(basePackage) ++ namespaces).filter(!_.isEmpty).reduceLeft { (a, b) => a + "." + b }
  }

  def toDirectoryPath(baseDir: String, namespaces: Seq[String]): String = {
    val dirs: Seq[String] = (Seq(baseDir) ++ namespaces).filter(!_.isEmpty)
    if (dirs.isEmpty) "" else dirs.reduceLeft { (a, b) => a + "/" + b }
  }

  def toControllerName(namespaces: Seq[String], resources: String): String = {
    if (namespaces.filterNot(_.isEmpty).isEmpty) toCamelCase(resources)
    else namespaces.head + namespaces.tail.map { n => n.head.toUpper + n.tail }.mkString + toClassName(resources)
  }

  def toControllerClassName(name: String) = toClassName(name) + "Controller"

  def toResourcesBasePath(namespaces: Seq[String]): String = {
    if (namespaces.filter(!_.isEmpty).isEmpty) ""
    else "/" + namespaces.filter(!_.isEmpty).reduceLeft { (a, b) => a + "/" + b }
  }

  def isOptionClassName(t: String): Boolean = t.trim().startsWith("Option")

  def extractTypeIfOptionOrSeq(t: String): String = {
    t.replaceFirst("Option\\[", "")
      .replaceFirst("Seq\\[", "")
      .replaceFirst("\\]", "")
      .trim()
  }

  def toCamelCase(v: String): String = StringUtil.toCamelCase(v)

  def toSnakeCase(v: String): String = StringUtil.toSnakeCase(v)

  def toSplitName(v: String): String = toSnakeCase(v).split("_").toSeq.mkString(" ")

  def toFirstCharUpper(s: String): String = s.head.toUpper + s.tail

  def toFirstCharLower(s: String): String = s.head.toLower + s.tail

  def toCapitalizedSplitName(v: String): String = {
    toSnakeCase(v).split("_").toSeq
      .map(word => word.head.toUpper + word.tail)
      .mkString(" ")
  }

  def extractColumns(tableName: String): List[Column] = {
    DB.getTable(tableName).map { table =>
      table.columns
    }.getOrElse {
      throw new IllegalStateException(s"Failed to retrieve meta data about columns for ${tableName}")
    }
  }

  def toScalaTypeName(paramTypeName: String): String = paramTypeName match {
    case "ByteArray" => "Array[Byte]"
    case "Option[ByteArray]" => "Option[Array[Byte]]"
    case _ => paramTypeName
  }

  def toScalaTypeNameWithDefaultValueIfOptionOrSeq(paramTypeName: String): String = {
    val scalaTypeName = toScalaTypeName(paramTypeName.trim())
    if (scalaTypeName.startsWith("Option")) s"${scalaTypeName} = None"
    else if (scalaTypeName.startsWith("Seq")) s"${scalaTypeName} = Nil"
    else scalaTypeName
  }

  def paramTypes = Seq(
    "Boolean",
    "Double",
    "Float",
    "Long",
    "Int",
    "Short",
    "String",
    "Byte",
    "ByteArray",
    "BigDecimal",
    "DateTime",
    "LocalDate",
    "LocalTime",
    "Option[Boolean]",
    "Option[Double]",
    "Option[Float]",
    "Option[Long]",
    "Option[Int]",
    "Option[Short]",
    "Option[String]",
    "Option[Byte]",
    "Option[ByteArray]",
    "Option[BigDecimal]",
    "Option[DateTime]",
    "Option[LocalDate]",
    "Option[LocalTime]"
  )

  def isSupportedParamType(typeName: String): Boolean = paramTypes.contains(typeName)

  def isAssociationTypeName(typeName: String): Boolean = {
    (typeName.startsWith("Option[") || typeName.startsWith("Seq[")) && !isSupportedParamType(typeName)
  }

  def toDBType(t: String): String = {
    extractTypeIfOptionOrSeq(t) match {
      case "String" => "varchar(512)"
      case "Long" => "bigint"
      case "Int" => "int"
      case "Short" => "int"
      case "Byte" => "tinyint"
      case "ByteArray" => "binary"
      case "BigDecimal" => "numeric"
      case "DateTime" => "timestamp"
      case "LocalDate" => "date"
      case "LocalTime" => "time"
      case "Boolean" => "boolean"
      case "Double" => "double"
      case "Float" => "float"
      case _ => "other"
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
