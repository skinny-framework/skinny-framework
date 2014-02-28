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

  private[this] def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:model member name:String birthday:Option[LocalDate]""")
    println("")
  }

  def run(args: List[String]) {
    args.toList match {
      case name :: attributes =>
        showSkinnyGenerator()
        val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(k, v) => Some(k -> v)
            case _ => None
          }
        }
        generate(name, None, attributePairs)
        println("")

      case _ => showUsage
    }
  }

  def code(name: String, tableName: Option[String], attributePairs: Seq[(String, String)]): String = {
    val modelClassName = toClassName(name)
    val timestampsTraitIfExists = if (withTimestamps) s"with TimestampsFeature[${modelClassName}] " else ""
    val timestamps = if (withTimestamps) {
      s""",
      |  createdAt: DateTime,
      |  updatedAt: Option[DateTime] = None""".stripMargin
    } else ""
    val timestampsExtraction = if (withTimestamps) {
      s""",
        |    createdAt = rs.get(rn.createdAt),
        |    updatedAt = rs.get(rn.updatedAt)""".stripMargin
    } else ""

    s"""package model
        |
        |import skinny.orm._, feature._
        |import scalikejdbc._, SQLInterpolation._
        |import org.joda.time._
        |
        |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
        |case class ${modelClassName}(
        |  id: Long,
        |${attributePairs.map { case (k, t) => s"  ${k}: ${addDefaultValueIfOption(t)}" }.mkString(",\n")}${timestamps}
        |)
        |
        |object ${modelClassName} extends SkinnyCRUDMapper[${modelClassName}] ${timestampsTraitIfExists}{
        |${tableName.map(t => "  override val tableName = \"" + t + "\"").getOrElse("")}
        |  override val defaultAlias = createAlias("${modelClassName.head.toLower}")
        |
        |  override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]): ${modelClassName} = new ${modelClassName}(
        |    id = rs.get(rn.id),
        |${attributePairs.map { case (k, t) => "    " + k + " = rs.get(rn." + k + ")" }.mkString(",\n")}${timestampsExtraction}
        |  )
        |}
        |""".stripMargin
  }

  def generate(name: String, tableName: Option[String], attributePairs: Seq[(String, String)]) {
    val productionFile = new File(s"src/main/scala/model/${toClassName(name)}.scala")
    writeIfAbsent(productionFile, code(name, tableName, attributePairs))
  }

  def spec(name: String): String = {
    s"""package model
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

  def generateSpec(name: String, attributePairs: Seq[(String, String)]) {
    val specFile = new File(s"src/test/scala/model/${toClassName(name)}Spec.scala")
    FileUtils.forceMkdir(specFile.getParentFile)
    writeIfAbsent(specFile, spec(name))
  }

}
