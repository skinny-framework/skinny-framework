package skinny.task.generator

import skinny.{ DBSettings, SkinnyEnv }
import scalikejdbc._
import scalikejdbc.metadata.Table

/**
 * Reverse Model All generator.
 */
object ReverseModelAllGenerator extends ReverseModelAllGenerator {
}

trait ReverseModelAllGenerator extends CodeGenerator {

  protected def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-model-all [env]""")
    println("")
  }

  protected def initializeDB(skinnyEnv: Option[String]): Unit = {
    System.setProperty(SkinnyEnv.PropertyKey, skinnyEnv.getOrElse(SkinnyEnv.Development))
    DBSettings.initialize()
  }

  def run(args: List[String]) {
    val skinnyEnv: Option[String] = args.headOption

    initializeDB(skinnyEnv)

    val tables: Seq[Table] = DB.getAllTableNames.filter(_.toLowerCase != "schema_version").flatMap { tableName =>
      DB.getTable(tableName)
    }
    val self = this
    val generator = new ReverseModelGenerator {
      override def cachedTables = tables
      override def useAutoConstruct = true
      override def createAssociationsForForeignKeys = true

      override def sourceDir = self.sourceDir
      override def testSourceDir = self.testSourceDir
      override def resourceDir = self.resourceDir
      override def testResourceDir = self.testResourceDir
      override def modelPackage = self.modelPackage
      override def modelPackageDir = self.modelPackageDir
    }
    tables.map { table =>
      val tableName = table.name.toLowerCase
      val args: List[String] = Seq(Some(tableName), Some(toCamelCase(tableName)), skinnyEnv).flatten.toList
      generator.run(args)
    }
  }

}
