package skinny.task.generator

import java.io.File
import scala.io.Source
import org.joda.time._

/**
 * Scaffold generator.
 */
trait ScaffoldGenerator extends CodeGenerator {

  protected def template: String = "ssp"

  private def showUsage = {
    println("Usage: sbt \"task/run generate-scaffold members member name:String birthday:Option[DateTime]\"")
  }

  private[this] def paramTypes = Seq(
    "Boolean",
    "Double",
    "Float",
    "Long",
    "Int",
    "Short",
    "String",
    "Byte",
    "ByteArray",
    "DateTime",
    "LocalDate",
    "LocalTime",
    "Option[Boolean]",
    "Option[Double]",
    "Option[Float]",
    "Option[Long]",
    "Option[Int]",
    "Option[Short]",
    "Option[String]",
    "Option[Byte]",
    "Option[ByteArray]",
    "Option[DateTime]",
    "Option[LocalDate]",
    "Option[LocalTime]"
  )

  private[this] def toSnakeCase(resources: String): String = {
    resources.map(c => if (c.isUpper) "_" + c.toLower else c)
      .mkString
      .replaceFirst("^_", "")
      .replaceFirst("_$", "")
      .replaceFirst("__", "_")
  }

  private[this] def toDBType(t: String): String = {
    toParamType(t) match {
      case "String" => "varchar(512)"
      case "Long" => "bigint"
      case "Int" => "int"
      case "Short" => "int"
      case "Byte" => "tinyint"
      case "ByteArray" => "binary"
      case "DateTime" => "timestamp"
      case "LocalDate" => "date"
      case "LocalTime" => "time"
      case "Boolean" => "boolean"
      case "Double" => "double"
      case "Float" => "float"
      case _ => "other"
    }
  }

  def run(args: List[String]) {
    if (args.size < 3) {
      showUsage
      return
    } else if (args.head.contains(":") || args.tail.head.contains(":")) {
      showUsage
      return
    }

    args match {
      case resources :: resource :: attributes =>
        val hasInvalidAttribute = attributes.exists { attr =>
          if (!attr.contains(":")) true
          else attr.split(":") match {
            case Array(_, paramType) => !paramTypes.contains(paramType)
            case _ => true
          }
        }
        if (hasInvalidAttribute) {
          showUsage
        } else {

          val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
            attribute.toString.split(":") match {
              case Array(k, v) => Some(k -> v)
              case _ => None
            }
          }
          // Controller
          generateResourceController(resources, resource, template, attributePairs)
          appendToScalatraBootstrap(resources)
          generateResourceControllerSpec(resources, resource, attributePairs)
          appendToFactoriesConf(resource, attributePairs)
          // Model
          ModelGenerator.generate(resource, Some(toSnakeCase(resources)), attributePairs)
          ModelGenerator.generateSpec(resource, attributePairs)
          // Views
          generateNewView(resources, resource, attributePairs)
          generateEditView(resources, resource, attributePairs)
          generateIndexView(resources, resource, attributePairs)
          generateShowView(resources, resource, attributePairs)
          // messages.conf
          generateMessages(resources, resource, attributePairs)
          // migration SQL
          generateMigrationSQL(resources, resource, attributePairs)
        }
      case _ => showUsage
    }
  }

  // --------------------------
  // Controller
  // --------------------------

  def controllerCode(resources: String, resource: String, template: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)
    val validations = attributePairs
      .filterNot { case (_, t) => isOptionClassName(t) }
      .filterNot { case (_, t) => toParamType(t) == "Boolean" }
      .map { case (k, t) => "    paramKey(\"" + k + "\") is required" }
      .mkString(",\n")

    s"""package controller
        |
        |import skinny._
        |import skinny.validator._
        |import model.${modelClassName}
        |
        |object ${controllerClassName} extends SkinnyResource {
        |  protectFromForgery()
        |${if (template != "ssp") "  override def scalateExtension = \"" + template + "\"" else ""}
        |  override def model = ${modelClassName}
        |  override def resourcesName = "${resources}"
        |  override def resourceName = "${resource}"
        |
        |  override def createForm = validation(
        |${validations}
        |  )
        |  override def createFormStrongParameters = Seq(
        |${attributePairs.map { case (k, t) => "    \"" + k + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
        |  )
        |
        |  override def updateForm = validation(
        |${validations}
        |  )
        |  override def updateFormStrongParameters = Seq(
        |${attributePairs.map { case (k, t) => "    \"" + k + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
        |  )
        |
        |}
        |""".stripMargin
  }

  def generateResourceController(resources: String, resource: String, template: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val file = new File(s"src/main/scala/controller/${controllerClassName}.scala")
    writeIfAbsent(file, controllerCode(resources, resource, template, attributePairs))
    println("\"" + file.getPath + "\" created.")
  }

  def controllerSpecCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)

    val params = attributePairs.map { case (k, t) => k -> toParamType(t) }.map {
      case (k, "Boolean") => "\"" + k + "\" -> \"true\""
      case (k, "DateTime") => "\"" + k + "\" -> new DateTime().toString()"
      case (k, "LocalDate") => "\"" + k + "\" -> new LocalDate().toString()"
      case (k, "LocalTime") => "\"" + k + "\" -> new LocalTime().toString()"
      case (k, _) => "\"" + k + "\" -> \"dummy\""
    }.mkString(",")

    s"""package controller
        |
        |import org.scalatra.test.scalatest._
        |import skinny._, test._
        |import org.joda.time._
        |import model._
        |
        |class ${controllerClassName}Spec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings {
        |  addFilter(${controllerClassName}, "/*")
        |
        |  def ${resource} = FactoryGirl(${modelClassName}).create()
        |
        |  it should "show ${resources}" in {
        |    get("/${resources}") {
        |      status should equal(200)
        |    }
        |    get("/${resources}/") {
        |      status should equal(200)
        |    }
        |    get("/${resources}.json") {
        |      status should equal(200)
        |    }
        |    get("/${resources}.xml") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show a ${resource} in detail" in {
        |    get(s"/${resources}/$${${resource}.id}") {
        |      status should equal(200)
        |    }
        |    get(s"/${resources}/$${${resource}.id}.xml") {
        |      status should equal(200)
        |    }
        |    get(s"/${resources}/$${${resource}.id}.json") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show new entry form" in {
        |    get(s"/${resources}/new") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "create a ${resource}" in {
        |    post(s"/${resources}", ${params}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrfToken" -> "12345") {
        |      post(s"/${resources}", ${params}, "csrfToken" -> "12345") {
        |        status should equal(302)
        |        val id = header("Location").split("/").last.toLong
        |        ${modelClassName}.findById(id).isDefined should equal(true)
        |      }
        |    }
        |  }
        |
        |  it should "show the edit form" in {
        |    get(s"/${resources}/$${${resource}.id}/edit") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "update a ${resource}" in {
        |    put(s"/${resources}/$${${resource}.id}", ${params}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrfToken" -> "12345") {
        |      put(s"/${resources}/$${${resource}.id}", ${params}, "csrfToken" -> "12345") {
        |        status should equal(200)
        |      }
        |    }
        |  }
        |
        |  it should "delete a ${resource}" in {
        |    val ${resource} = FactoryGirl(${modelClassName}).create()
        |    delete(s"/${resources}/$${${resource}.id}") {
        |      status should equal(403)
        |    }
        |    withSession("csrfToken" -> "aaaaaa") {
        |      delete(s"/${resources}/$${${resource}.id}?csrfToken=aaaaaa") {
        |        status should equal(200)
        |      }
        |    }
        |  }
        |
        |}
        |""".stripMargin
  }

  def generateResourceControllerSpec(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val file = new File(s"src/test/scala/controller/${controllerClassName}Spec.scala")
    writeIfAbsent(file, controllerSpecCode(resources, resource, attributePairs))
  }

  // --------------------------
  // ScalatraBootstrap.scala
  // --------------------------

  def appendToScalatraBootstrap(resources: String) {
    val controllerClassName = toClassName(resources) + "Controller"
    val newCode =
      s"""override def initSkinnyApp(ctx: ServletContext) {
        |    ${controllerClassName}.mount(ctx)
        |""".stripMargin
    val file = new File("src/main/scala/ScalatraBootstrap.scala")
    if (file.exists()) {
      val code = Source.fromFile(file).mkString.replaceFirst(
        "(override\\s+def\\s+initSkinnyApp\\s*\\(ctx:\\s+ServletContext\\)\\s*\\{)", newCode)
      forceWrite(file, code)
    } else {
      val fullNewCode =
        """import _root_.controller._
          |import skinny._
          |
          |class ScalatraBootstrap extends SkinnyLifeCycle {
          |  ${newCode}
          |  }
          |}
          |""".stripMargin
      forceWrite(file, fullNewCode)
    }
  }

  // --------------------------
  // factories.conf
  // --------------------------

  def appendToFactoriesConf(resource: String, attributePairs: Seq[(String, String)]) {
    val file = new File(s"src/test/resources/factories.conf")
    val params = attributePairs.map { case (k, t) => k -> toParamType(t) }.map {
      case (k, "Boolean") => "  " + k + "=true"
      case (k, "LocalDate") => "  " + k + "=\"" + new LocalDate().toString() + "\""
      case (k, "LocalTime") => "  " + k + "=\"" + new LocalTime().toString() + "\""
      case (k, "DateTime") => "  " + k + "=\"" + new DateTime().toString() + "\""
      case (k, _) => "  " + k + "=\"Something New\""
    }.mkString("\n")

    val code =
      s"""${resource} {
          |${params}
          |}
          |""".stripMargin

    writeAppending(file, code)
  }

  // --------------------------
  // messages.conf
  // --------------------------

  def messagesConfCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val _resources = toClassName(resources)
    val _resource = toClassName(resource)

    s"""
        |${resource} {
        |  flash {
        |    created="The ${resource} was created."
        |    updated="The ${resource} was updated."
        |    deleted="The ${resource} was deleted."
        |  }
        |  list="${_resources}"
        |  detail="${_resource}"
        |  edit="Edit ${_resource}"
        |  new="New ${_resource}"
        |  delete.confirm="Are you sure?"
        |  id="ID"
        |${attributePairs.map { case (k, _) => "  " + k + "=\"" + toClassName(k) + "\"" }.mkString("\n")}
        |}
        |""".stripMargin
  }

  def generateMessages(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val file = new File(s"src/main/resources/messages.conf")
    writeAppending(file, messagesConfCode(resources, resource, attributePairs))
  }

  // --------------------------
  // Flyway migration SQL
  // --------------------------

  def migrationSQL(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val name = toSnakeCase(resources)
    val columns = attributePairs.map {
      case (k, t) =>
        s"  ${toSnakeCase(k)} ${toDBType(t)}" + (if (isOptionClassName(t)) "" else " not null")
    }.mkString(",\n")
    s"""-- For H2 Database
        |create table ${name} (
        |  id bigserial not null primary key,
        |${columns},
        |  created_at timestamp not null,
        |  updated_at timestamp
        |)
        |""".stripMargin
  }

  def generateMigrationSQL(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val version = DateTime.now.toString("yyyyMMddHHmmss")
    val file = new File(s"src/main/resources/db/migration/V${version}__Create_${resources}_table.sql")
    writeIfAbsent(file, migrationSQL(resources, resource, attributePairs))
  }

  // --------------------------
  // Views
  // --------------------------

  def generateNewView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateEditView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateIndexView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???

}

