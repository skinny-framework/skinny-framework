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
    println("""         sbt "task/run generate:reverse-scaffold table_name namespace.resources resource" """)
    println("""         sbt "task/run generate:reverse-scaffold table_name namespace resources resource" """)
    println()
  }

  override def showSkinnyGenerator() = {
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
        if (args.size >= 4) {
          (args(0), args(1), args(2), args(3))
        } else if (args.size == 3 && args(1).contains(".")) {
          val elements = args(1).split("\\.")
          (args(0), elements.init.mkString("."), elements.last, args(2))
        } else {
          (args(0), "", args(1), args(2))
        }
      }

      System.setProperty(SkinnyEnv.PropertyKey, skinnyEnv.getOrElse(SkinnyEnv.Development))
      DBSettings.initialize()

      val columns = extractColumns(tableName)
      val hasId = if (columns.find(_.isPrimaryKey).isEmpty) {
        println(
          s"""
            |Since this table (${tableName}) has no primary key, generator created only NoIdCRUDMapper file and skipped creating controller and view files.""".stripMargin)
        false
      } else if (columns.filter(_.isPrimaryKey).size > 1) {
        println(
          s"""
            |Since this table (${tableName}) has compound primary keys, generator created only NoIdCRUDMapper file and skipped creating controller and view files.""".stripMargin)
        false
      } else {
        true
      }
      val pkName: Option[String] = if (hasId) columns.find(_.isPrimaryKey).map(_.name.toLowerCase(Locale.ENGLISH)).map(toCamelCase) else None
      val pkType: Option[ParamType] = if (hasId) columns.find(_.isPrimaryKey).map(_.typeCode).map(convertJdbcSqlTypeToParamType) else None
      val fields: List[String] = if (hasId) {
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
        |  ID        : ${pkName.getOrElse("")}:${pkType.getOrElse("")}
        |  Resources : ${resources}
        |  Resource  : ${resource}
        |
        |  Columns:
        |${fields.map(f => s"   - ${f}").mkString("\n")}""".stripMargin
      } else {
        s"""
        | *** Skinny Reverse Engineering Task ***
        |
        |  Table  : ${tableName}
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

}

