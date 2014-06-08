package skinny.task.generator

import skinny._
import scalikejdbc._
import scalikejdbc.metadata.Column
import skinny.ParamType._
import java.sql.Types._
import java.util.Locale

object ReverseScaffoldGenerator extends ReverseScaffoldGenerator

/**
 * Skinny Reverse Generator Task.
 */
trait ReverseScaffoldGenerator extends CodeGenerator {

  protected def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-scaffold table_name resources resource" """)
    println("""         sbt "task/run generate:reverse-scaffold table_name namespace resources resource" """)
    println()
  }

  protected override def showSkinnyGenerator() = {
    println("""
 *** Skinny Reverse Engineering Task ***
""")
  }

  def run(templateType: String, args: List[String], skinnyEnv: Option[String] = None): Unit = {
    if (args.size < 3) {
      showUsage
      return
    }
    try {
      val (tableName, namespace, resources, resource) = {
        if (args.size >= 4) (args(0), args(1), args(2), args(3))
        else (args(0), "", args(1), args(2))
      }

      System.setProperty(SkinnyEnv.PropertyKey, skinnyEnv.getOrElse(SkinnyEnv.Development))
      DBSettings.initialize()

      val columns = extractColumns(tableName)
      val hasId = if (columns.find(_.isPrimaryKey).isEmpty) {
        println(
          s"""
            |Since this table (${tableName}) has no primary key, a NoIdCRUDMapper model is created.
            |A controller and a view are not created.
            |""".stripMargin)
        false
      } else if (columns.filter(_.isPrimaryKey).size > 1) {
        println(
          s"""
            |Since this table (${tableName}) has composite primary key, a NoIdCRUDMapper model is created.
            |A controller and a view are not created.
            |""".stripMargin)
        false
      } else {
        true
      }
      val pkName = if (hasId) {
        Some(toCamelCase(columns.find(_.isPrimaryKey).get.name.toLowerCase(Locale.ENGLISH)))
      } else {
        None
      }
      val pkType = if (hasId) {
        Some(convertJdbcSqlTypeToParamType(columns.find(_.isPrimaryKey).get.typeCode))
      } else {
        None
      }
      val fields = if (hasId) {
        columns
          .map(column => toScaffoldFieldDef(column))
          .filter(param => param != "id:Long" && param != "id:Option[Long]")
          .filter(param => !param.startsWith(pkName.get + ":"))
          .map(param => toCamelCase(param))
      } else {
        columns
          .map(column => toScaffoldFieldDef(column))
          .map(param => toCamelCase(param))
      }

      println(if (hasId) {
        s"""
        | *** Skinny Reverse Engineering Task ***
        |
        |  Table     : ${tableName}
        |  ID        : ${pkName}:${pkType}
        |  Resources : ${resources}
        |  Resource  : ${resource}
        |
        |  Columns:
        |${fields.map(f => s"   - ${f}").mkString("\n")}""".stripMargin
      } else {
        s"""
        | *** Skinny Reverse Engineering Task ***
        |
        |  Table     : ${tableName}
        |
        |  Columns:
        |${fields.map(f => s"   - ${f}").mkString("\n")}""".stripMargin
      })

      val table = tableName
      val generator = templateType match {
        case "ssp" => new ScaffoldSspGenerator {
          override def withId = hasId
          override def primaryKeyName = pkName.getOrElse(super.primaryKeyName)
          override def primaryKeyType = pkType.getOrElse(super.primaryKeyType)
          override def withTimestamps: Boolean = false
          override def skipDBMigration = true
          override def tableName = Some(table)
        }
        case "scaml" => new ScaffoldScamlGenerator {
          override def withId = hasId
          override def primaryKeyName = pkName.getOrElse(super.primaryKeyName)
          override def primaryKeyType = pkType.getOrElse(super.primaryKeyType)
          override def withTimestamps: Boolean = false
          override def skipDBMigration = true
          override def tableName = Some(table)
        }
        case "jade" => new ScaffoldJadeGenerator {
          override def withId = hasId
          override def primaryKeyName = pkName.getOrElse(super.primaryKeyName)
          override def primaryKeyType = pkType.getOrElse(super.primaryKeyType)
          override def withTimestamps: Boolean = false
          override def skipDBMigration = true
          override def tableName = Some(table)
        }
        case _ => throw new IllegalArgumentException("Unknown template type: " + templateType)
      }
      generator.run(Seq(namespace, resources, resource) ++ fields)

    } catch {
      case e: Exception =>
        showErrors(Seq(e.getMessage))
    }
  }

  def extractColumns(tableName: String): List[Column] = {
    DB.getTable(tableName).map { table =>
      table.columns
    }.getOrElse {
      throw new IllegalStateException(s"Failed to retrieve meta data about columns for ${tableName}")
    }
  }

  private[this] def toScaffoldFieldDef(column: Column): String = {
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

  private[this] def toParamType(column: Column): ParamType = convertJdbcSqlTypeToParamType(column.typeCode)

  private[this] def convertJdbcSqlTypeToParamType(dataType: Int): ParamType = dataType match {
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

}

