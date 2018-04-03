package skinny.task.generator

import scalikejdbc.NamedDB
import skinny.{ DBSettings, SkinnyEnv }
import scalikejdbc.metadata.Table
import skinny.nlp.Inflector

/**
  * Reverse Model All generator.
  */
object ReverseModelAllGenerator extends ReverseModelAllGenerator {}

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

  def run(args: List[String]): Unit = {
    val skinnyEnv: Option[String] = args.headOption

    initializeDB(skinnyEnv)

    val tables: Seq[Table] = NamedDB(connectionPoolName)
      .getTableNames("%")
      .filter(_.toLowerCase != "schema_version")
      .flatMap { tableName =>
        NamedDB(connectionPoolName).getTable(tableName)
      }
    val self             = this
    val skipInitializeDB = (env: Option[String]) => {}

    val generator = new ReverseModelGenerator {
      override def cachedTables                     = tables
      override def useAutoConstruct                 = true
      override def createAssociationsForForeignKeys = true
      override def connectionPoolName               = self.connectionPoolName

      override def sourceDir                               = self.sourceDir
      override def testSourceDir                           = self.testSourceDir
      override def resourceDir                             = self.resourceDir
      override def testResourceDir                         = self.testResourceDir
      override def modelPackage                            = self.modelPackage
      override def modelPackageDir                         = self.modelPackageDir
      override def initializeDB(skinnyEnv: Option[String]) = skipInitializeDB(skinnyEnv)
    }
    tables.map { table =>
      val tableName          = table.name.toLowerCase
      val className          = Inflector.singularize(toCamelCase(tableName))
      val args: List[String] = Seq(Some(tableName), Some(className), skinnyEnv).flatten.toList
      generator.run(args)
    }
  }

}
