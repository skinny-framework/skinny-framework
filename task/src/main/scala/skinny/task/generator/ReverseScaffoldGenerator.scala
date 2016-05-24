package skinny.task.generator

import skinny._
import scalikejdbc.metadata.{ Table }
import java.util.Locale

object ReverseScaffoldGenerator extends ReverseScaffoldGenerator

/**
 * Skinny Reverse Generator Task.
 */
trait ReverseScaffoldGenerator extends CodeGenerator with ReverseGenerator {

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

  def cachedTables: Seq[Table] = Nil

  def useAutoConstruct: Boolean = false

  def createAssociationsForForeignKeys: Boolean = false

  def run(templateType: String, args: List[String], skinnyEnv: Option[String]): Unit = {
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
            |Since this table (${tableName}) has no primary key, generator created only NoIdCRUDMapper file and skipped creating controller and view files.""".stripMargin
        )
        false
      } else if (columns.filter(_.isPrimaryKey).size > 1) {
        println(
          s"""
            |Since this table (${tableName}) has compound primary keys, generator created only NoIdCRUDMapper file and skipped creating controller and view files.""".stripMargin
        )
        false
      } else {
        true
      }
      val pkName: Option[String] = if (hasId) columns.find(_.isPrimaryKey).map(_.name.toLowerCase(Locale.ENGLISH)).map(toCamelCase) else None
      val pkType: Option[ParamType] = if (hasId) {
        columns.find(_.isPrimaryKey).map(_.typeCode).map { code =>
          convertJdbcSqlTypeToParamType(code) match {
            case ParamType.Int => ParamType.Long // auto boxing issue
            case t => t
          }
        }
      } else None
      val fields: List[String] = {
        val fields: List[String] = {
          if (hasId) {
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
        }
        if (createAssociationsForForeignKeys) {
          fields ++ extractAssociationParams(tableName, pkName, cachedTables)
        } else {
          fields
        }
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
      val self = this
      val generator = templateType match {
        case "ssp" => new ScaffoldSspGenerator {
          override def withId = hasId
          override def primaryKeyName = pkName.getOrElse(super.primaryKeyName)
          override def primaryKeyType = pkType.getOrElse(super.primaryKeyType)
          override def withTimestamps: Boolean = false
          override def useAutoConstruct = self.useAutoConstruct
          override def skipDBMigration = true
          override def tableName = Some(table)

          override def sourceDir = self.sourceDir
          override def testSourceDir = self.testSourceDir
          override def resourceDir = self.resourceDir
          override def testResourceDir = self.testResourceDir
          override def webInfDir = self.webInfDir
          override def controllerPackage = self.controllerPackage
          override def controllerPackageDir = self.controllerPackageDir
          override def modelPackage = self.modelPackage
          override def modelPackageDir = self.modelPackageDir
        }
        case "scaml" => new ScaffoldScamlGenerator {
          override def withId = hasId
          override def primaryKeyName = pkName.getOrElse(super.primaryKeyName)
          override def primaryKeyType = pkType.getOrElse(super.primaryKeyType)
          override def withTimestamps: Boolean = false
          override def useAutoConstruct = self.useAutoConstruct
          override def skipDBMigration = true
          override def tableName = Some(table)

          override def sourceDir = self.sourceDir
          override def testSourceDir = self.testSourceDir
          override def resourceDir = self.resourceDir
          override def testResourceDir = self.testResourceDir
          override def webInfDir = self.webInfDir
          override def controllerPackage = self.controllerPackage
          override def controllerPackageDir = self.controllerPackageDir
          override def modelPackage = self.modelPackage
          override def modelPackageDir = self.modelPackageDir
        }
        case "jade" => new ScaffoldJadeGenerator {
          override def withId = hasId
          override def primaryKeyName = pkName.getOrElse(super.primaryKeyName)
          override def primaryKeyType = pkType.getOrElse(super.primaryKeyType)
          override def withTimestamps: Boolean = false
          override def useAutoConstruct = self.useAutoConstruct
          override def skipDBMigration = true
          override def tableName = Some(table)

          override def sourceDir = self.sourceDir
          override def testSourceDir = self.testSourceDir
          override def resourceDir = self.resourceDir
          override def testResourceDir = self.testResourceDir
          override def webInfDir = self.webInfDir
          override def controllerPackage = self.controllerPackage
          override def controllerPackageDir = self.controllerPackageDir
          override def modelPackage = self.modelPackage
          override def modelPackageDir = self.modelPackageDir
        }
        case _ => throw new IllegalArgumentException("Unknown template type: " + templateType)
      }
      generator.run(Seq(namespace, resources, resource) ++ fields)

    } catch {
      case scala.util.control.NonFatal(e) => showErrors(Seq(e.getMessage))
    }
  }

}

