package skinny.task.generator

import java.util.Locale
import skinny.{ DBSettings, SkinnyEnv, ParamType }
import scalikejdbc.metadata.{ Table, Column }

/**
 * Reverse Model generator.
 */
object ReverseModelGenerator extends ReverseModelGenerator {
}

trait ReverseModelGenerator extends CodeGenerator with ReverseGenerator {

  def withId: Boolean = true

  def primaryKeyName: String = "id"

  def primaryKeyType: ParamType = ParamType.Long

  def cachedTables: Seq[Table] = Nil

  def useAutoConstruct: Boolean = false

  def createAssociationsForForeignKeys: Boolean = false

  protected def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-model table_name [className] [env]""")
    println("")
  }

  protected def initializeDB(skinnyEnv: Option[String]): Unit = {
    System.setProperty(SkinnyEnv.PropertyKey, skinnyEnv.getOrElse(SkinnyEnv.Development))
    DBSettings.initialize()
  }

  def run(args: List[String]) {
    val (tableName: String, nameWithPackage: Option[String], skinnyEnv: Option[String]) = args match {
      case tableName :: Nil => (tableName, None, None)
      case tableName :: nameWithPackage :: Nil => (tableName, Some(nameWithPackage), None)
      case tableName :: nameWithPackage :: env :: Nil => (tableName, Some(nameWithPackage), Some(env))
      case _ =>
        showUsage
        return
    }

    initializeDB(skinnyEnv)

    val columns: List[Column] = extractColumns(tableName)

    val hasId: Boolean = columns.filter(_.isPrimaryKey).size == 1
    val pkName: Option[String] = if (hasId) columns.find(_.isPrimaryKey).map(_.name.toLowerCase(Locale.ENGLISH)).map(toCamelCase) else None
    val pkType: Option[ParamType] = if (hasId) columns.find(_.isPrimaryKey).map(_.typeCode).map(convertJdbcSqlTypeToParamType) else None
    val fields: Seq[String] = {
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

    val (self, _tableName) = (this, tableName)
    val generator = new ModelGenerator {
      override def withId = hasId
      override def primaryKeyName = pkName.getOrElse(self.primaryKeyName)
      override def primaryKeyType = pkType.getOrElse(self.primaryKeyType)
      override def withTimestamps: Boolean = false
      override def useAutoConstruct = self.useAutoConstruct
      override def tableName = Some(_tableName)

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

    val _nameWithPackage = nameWithPackage.getOrElse(toClassName(tableName.toLowerCase))
    val namespace = _nameWithPackage.split("\\.").init.mkString(".")
    val name = _nameWithPackage.split("\\.").last
    generator.run(Seq(namespace, name) ++ fields)

  }

}
