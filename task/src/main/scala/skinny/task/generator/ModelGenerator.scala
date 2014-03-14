package skinny.task.generator

import java.io.File
import org.apache.commons.io.FileUtils

/**
 * Model generator.
 */
object ModelGenerator extends ModelGenerator {
  override def withTimestamps: Boolean = true
}
object ModelWithoutTimestampsGenerator extends ModelGenerator {
  override def withTimestamps: Boolean = false
}

trait ModelGenerator extends CodeGenerator {

  def withTimestamps: Boolean = true

  def primaryKeyName: String = "id"

  private[this] def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:model member name:String birthday:Option[LocalDate]""")
    println("""         sbt "task/run generate:model admin.legacy member name:String birthday:Option[LocalDate]""")
    println("")
  }

  def run(args: List[String]) {
    val completedArgs: Seq[String] = if (args.size >= 2 && args(1).contains(":")) Seq("") ++ args
    else args
    completedArgs.toList match {
      case namespace :: name :: attributes =>
        showSkinnyGenerator()
        val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(k, v) => Some(k -> v)
            case _ => None
          }
        }
        generate(namespace.split('.'), name, None, attributePairs)
        println("")

      case _ => showUsage
    }
  }

  def code(namespaces: Seq[String], name: String, tableName: Option[String], attributePairs: Seq[(String, String)]): String = {
    val namespace = toNamespace("model", namespaces)
    val modelClassName = toClassName(name)
    val alias = modelClassName.filter(_.isUpper).map(_.toLower).mkString
    val timestampsTraitIfExists = if (withTimestamps) s"with TimestampsFeature[${modelClassName}] " else ""
    val timestamps = if (withTimestamps) {
      s""",
         |  createdAt: DateTime,
         |  updatedAt: DateTime""".stripMargin
    } else ""
    val timestampsExtraction = if (withTimestamps) {
      s""",
         |    createdAt = rs.get(rn.createdAt),
         |    updatedAt = rs.get(rn.updatedAt)""".stripMargin
    } else ""
    val customPkName = {
      if (primaryKeyName != "id") "\n  override lazy val primaryKeyFieldName = \"" + primaryKeyName + "\""
      else ""
    }

    val classFields =
      s"""  ${primaryKeyName}: Long${
        if (attributePairs.isEmpty) ""
        else attributePairs.map {
          case (k, t) =>
            s"  ${k}: ${toScalaTypeNameWithDefaultValueIfOption(t)}"
        }.mkString(",\n", ",\n", "")
      }${timestamps}
        |""".stripMargin

    val extractors =
      s"""    ${primaryKeyName} = rs.get(rn.${primaryKeyName})${
        if (attributePairs.isEmpty) ""
        else attributePairs.map {
          case (k, t) =>
            "    " + k + " = rs.get(rn." + k + ")"
        }.mkString(",\n", ",\n", "")
      }${timestampsExtraction}
        |""".stripMargin

    s"""package ${namespace}
        |
        |import skinny.orm._, feature._
        |import scalikejdbc._, SQLInterpolation._
        |import org.joda.time._
        |
        |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
        |case class ${modelClassName}(
        |${classFields})
        |
        |object ${modelClassName} extends SkinnyCRUDMapper[${modelClassName}] ${timestampsTraitIfExists}{
        |${tableName.map(t => "  override lazy val tableName = \"" + t + "\"").getOrElse("")}
        |  override lazy val defaultAlias = createAlias("${alias}")${customPkName}
        |
        |  override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]): ${modelClassName} = new ${modelClassName}(
        |${extractors}  )
        |}
        |""".stripMargin
  }

  def generate(namespaces: Seq[String], name: String, tableName: Option[String], attributePairs: Seq[(String, String)]) {
    val productionFile = new File(s"src/main/scala/${toDirectoryPath("model", namespaces)}/${toClassName(name)}.scala")
    writeIfAbsent(productionFile, code(namespaces, name, tableName, attributePairs))
  }

  def spec(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace("model", namespaces)}
        |
        |import skinny.test._
        |import org.scalatest.fixture.FlatSpec
        |import scalikejdbc._, SQLInterpolation._
        |import scalikejdbc.scalatest._
        |import org.joda.time._
        |
        |class ${toClassName(name)}Spec extends FlatSpec with AutoRollback {
        |}
        |""".stripMargin
  }

  def generateSpec(namespaces: Seq[String], name: String, attributePairs: Seq[(String, String)]) {
    val specFile = new File(s"src/test/scala/${toDirectoryPath("model", namespaces)}/${toClassName(name)}Spec.scala")
    FileUtils.forceMkdir(specFile.getParentFile)
    writeIfAbsent(specFile, spec(namespaces, name))
  }

}
