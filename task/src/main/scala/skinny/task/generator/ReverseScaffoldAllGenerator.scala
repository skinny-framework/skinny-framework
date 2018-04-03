package skinny.task.generator

import java.util.Locale

import skinny.{ DBSettings, SkinnyEnv }
import scalikejdbc.NamedDB
import scalikejdbc.metadata.Table
import skinny.nlp.Inflector

/**
  * Reverse Model All generator.
  */
object ReverseScaffoldAllGenerator extends ReverseScaffoldAllGenerator {}

trait ReverseScaffoldAllGenerator extends CodeGenerator {

  def namespace: Option[String] = None

  def descendingOrderForIndexPage: Boolean = false

  def operationLinksInIndexPageRequired: Boolean = true

  // if empty, all tables are targets to generate scaffold
  def targetTableNames: Seq[String] = Seq.empty

  def tableNamesToBeExcluded: Seq[String] = Seq.empty

  protected def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-model-all [env]""")
    println("")
  }

  protected def initializeDB(skinnyEnv: Option[String]): Unit = {
    System.setProperty(SkinnyEnv.PropertyKey, skinnyEnv.getOrElse(SkinnyEnv.Development))
    DBSettings.initialize()
  }

  def run(templateType: String, args: List[String], skinnyEnv: Option[String]): Unit = {

    initializeDB(skinnyEnv)

    val tables: Seq[Table] = NamedDB(connectionPoolName)
      .getTableNames("%")
      .filter(_.toLowerCase != "schema_version")
      .flatMap { tableName =>
        NamedDB(connectionPoolName).getTable(tableName)
      }

    val self = this
    val generator = new ReverseScaffoldGenerator {
      override def cachedTables                      = tables
      override def useAutoConstruct                  = true
      override def createAssociationsForForeignKeys  = true
      override def connectionPoolName                = self.connectionPoolName
      override def descendingOrderForIndexPage       = self.descendingOrderForIndexPage
      override def operationLinksInIndexPageRequired = self.operationLinksInIndexPageRequired

      override def sourceDir            = self.sourceDir
      override def testSourceDir        = self.testSourceDir
      override def resourceDir          = self.resourceDir
      override def testResourceDir      = self.testResourceDir
      override def webInfDir            = self.webInfDir
      override def controllerPackage    = self.controllerPackage
      override def controllerPackageDir = self.controllerPackageDir
      override def modelPackage         = self.modelPackage
      override def modelPackageDir      = self.modelPackageDir
    }

    val whiteList = targetTableNames.map(_.toLowerCase(Locale.ENGLISH))
    val blackList = tableNamesToBeExcluded.map(_.toLowerCase(Locale.ENGLISH))

    tables
      .filter { table =>
        if (whiteList.isEmpty) true
        else whiteList.contains(table.name.toLowerCase(Locale.ENGLISH))
      }
      .filter { table =>
        if (blackList.isEmpty) true
        else blackList.contains(table.name.toLowerCase(Locale.ENGLISH)) == false
      }
      .map { table =>
        val tableName = table.name.toLowerCase
        val className = toNormalizedEntityName(tableName, tables)
        val args: List[String] =
          List(Some(tableName), namespace, Some(Inflector.pluralize(className)), Some(className)).flatten
        generator.run(templateType, args, SkinnyEnv.get())
      }
  }

  private def toNormalizedEntityName(tableName: String, tables: Seq[Table]): String = {
    val _tableName = tableName.toLowerCase(Locale.ENGLISH)
    if (isJoinTable(_tableName, tables)) {
      // normalize join table entity name
      val joinedTableNames = tables
        .map(_.name.toLowerCase(Locale.ENGLISH))
        .filter(t => _tableName != t && (_tableName.startsWith(t) || _tableName.endsWith(t)))

      val normalizedName = joinedTableNames.foldLeft(_tableName.toLowerCase(Locale.ENGLISH)) {
        case (table, joined) => tableName.replaceFirst(joined, Inflector.singularize(joined))
      }
      Inflector.singularize(toClassName(normalizedName))
    } else {
      Inflector.singularize(toClassName(_tableName))
    }
  }

  private def isJoinTable(tableName: String, tables: Seq[Table]): Boolean = {
    val _tableName = tableName.toLowerCase(Locale.ENGLISH)
    tables.exists(
      t =>
        _tableName.startsWith(t.name.toLowerCase(Locale.ENGLISH)) ||
        _tableName.endsWith(t.name.toLowerCase(Locale.ENGLISH))
    )
  }

}
