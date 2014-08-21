package skinny.task.generator

import skinny.ParamType
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

  def withId: Boolean = true

  def withTimestamps: Boolean = true

  def primaryKeyName: String = "id"

  def primaryKeyType: ParamType = ParamType.Long

  def tableName: Option[String] = None

  private[this] def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:model member name:String birthday:Option[LocalDate]""")
    println("""         sbt "task/run generate:model admin.legacy member name:String birthday:Option[LocalDate]""")
    println("")
  }

  def run(args: Seq[String]) {
    val completedArgs: List[String] = {
      if (args.size >= 2 && args(1).contains(":")) Seq("") ++ args
      else args
    }.toList
    completedArgs match {
      case namespace :: name :: attributes =>
        showSkinnyGenerator()
        val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(k, v, columnDef) => Some(k -> v)
            case Array(k, v) => Some(k -> v)
            case _ => None
          }
        }
        generate(namespace.split('.'), name, tableName, attributePairs)
        generateSpec(namespace.split('.'), name, attributePairs)
        println("")

      case _ => showUsage
    }
  }

  def code(namespaces: Seq[String], name: String, tableName: Option[String], attributePairs: Seq[(String, String)]): String = {
    val namespace = toNamespace("model", namespaces)
    val modelClassName = toClassName(name)
    val alias = modelClassName.filter(_.isUpper).map(_.toLower).mkString
    val timestampPrefix = if (withId) ",\n" else { if (attributePairs.isEmpty) "" else ",\n" }
    val classFieldsPrimaryKeyRow = if (withId) s"""  ${primaryKeyName}: ${primaryKeyType}""" else ""
    val extractorsPrimaryKeyRow = if (withId) s"""    ${primaryKeyName} = rs.get(rn.${primaryKeyName})""" else ""
    val attributePrefix = if (withId) ",\n" else ""
    val mapperClassName = if (withId) "SkinnyCRUDMapper" else "SkinnyNoIdCRUDMapper"
    val timestampsTraitIfExists = if (withTimestamps) s"with TimestampsFeature[${modelClassName}] " else ""

    val timestamps = if (withTimestamps) {
      s"""${timestampPrefix}  createdAt: DateTime,
         |  updatedAt: DateTime""".stripMargin
    } else ""

    val timestampsExtraction = if (withTimestamps) {
      s"""${timestampPrefix}    createdAt = rs.get(rn.createdAt),
         |    updatedAt = rs.get(rn.updatedAt)""".stripMargin
    } else ""

    val customPkName = {
      if (primaryKeyName != "id") "\n  override lazy val primaryKeyFieldName = \"" + primaryKeyName + "\""
      else ""
    }

    val classFields = s"""${classFieldsPrimaryKeyRow}${
      if (attributePairs.isEmpty) ""
      else attributePairs.map {
        case (k, t) =>
          s"  ${k}: ${toScalaTypeNameWithDefaultValueIfOption(t)}"
      }.mkString(attributePrefix, ",\n", "")
    }${timestamps}
        |""".stripMargin

    val extractors =
      s"""${extractorsPrimaryKeyRow}${
        if (attributePairs.isEmpty) ""
        else attributePairs.map {
          case (k, t) =>
            "    " + k + " = rs.get(rn." + k + ")"
        }.mkString(attributePrefix, ",\n", "")
      }${timestampsExtraction}
        |""".stripMargin

    val primaryKeyTypeIfNotLong = if (primaryKeyType == ParamType.Long) "" else
      s"""
         |  override def idToRawValue(id: String): Any = id
         |  override def rawValueToId(value: Any): String = value.toString
         |  override def useExternalIdGenerator = true
         |  override def generateId = java.util.UUID.randomUUID.toString""".stripMargin

    s"""package ${namespace}
        |
        |import skinny.orm._, feature._
        |import scalikejdbc._
        |import org.joda.time._
        |
        |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
        |case class ${modelClassName}(
        |${classFields})
        |
        |object ${modelClassName} extends ${mapperClassName}${if (primaryKeyType == ParamType.Long) s"[${modelClassName}]" else s"WithId[${primaryKeyType}, ${modelClassName}]"} ${timestampsTraitIfExists}{
        |${tableName.map(t => "  override lazy val tableName = \"" + t + "\"").getOrElse("")}
        |  override lazy val defaultAlias = createAlias("${alias}")${customPkName}${primaryKeyTypeIfNotLong}
        |
        |  /*
        |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
        |   * (e.g.)
        |   * override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]) = autoConstruct(rs, rn)
        |   *
        |   * Be aware of excluding associations like this:
        |   * (e.g.)
        |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
        |   * object Member extends SkinnyCRUDMapper[Member] {
        |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
        |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
        |   * }
        |   */
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
        |import skinny.DBSettings
        |import skinny.test._
        |import org.scalatest.fixture.FlatSpec
        |import org.scalatest._
        |import scalikejdbc._
        |import scalikejdbc.scalatest._
        |import org.joda.time._
        |
        |class ${toClassName(name)}Spec extends FlatSpec with Matchers with DBSettings with AutoRollback {
        |}
        |""".stripMargin
  }

  def generateSpec(namespaces: Seq[String], name: String, attributePairs: Seq[(String, String)]) {
    val specFile = new File(s"src/test/scala/${toDirectoryPath("model", namespaces)}/${toClassName(name)}Spec.scala")
    FileUtils.forceMkdir(specFile.getParentFile)
    writeIfAbsent(specFile, spec(namespaces, name))
  }

}
