package skinny.task

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Scaffold generator.
 */
trait ScaffoldGenerator extends CodeGenerator {

  private def showUsage = {
    println("Usage: sbt \"task/run scaffold members member name:String birthday:Option[DateTime]\"")
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
        val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(k, v) => Some(k -> v)
            case _ => None
          }
        }
        // Controller
        generateResourceController(resources, resource, attributePairs)
        generateResourceControllerSpec(resources, resource, attributePairs)

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

        // SQL
        generateSQLs(resources, resource, attributePairs)

      case _ =>
        showUsage
    }
  }

  def generateResourceController(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)
    val file = new File(s"src/main/scala/controller/${controllerClassName}.scala")
    val code =
      s"""package controller
        |
        |import skinny._
        |import skinny.validator._
        |import model.${modelClassName}
        |
        |object ${controllerClassName} extends SkinnyResource {
        |  protectFromForgery()
        |
        |  override def model = ${modelClassName}
        |  override def resourcesName = "${resources}"
        |  override def resourceName = "${resource}"
        |
        |  override def createForm = validation(
        |${attributePairs.filterNot { case (_, t) => isOptionClassName(t) }.map { case (k, t) => "    paramKey(\"" + k + "\") is required" }.mkString(",\n")}
        |  )
        |  override def createFormStrongParameters = Seq(
        |${attributePairs.map { case (k, t) => "    \"" + k + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
        |  )
        |
        |  override def updateForm = validation(
        |${attributePairs.filterNot { case (_, t) => isOptionClassName(t) }.map { case (k, t) => "    paramKey(\"" + k + "\") is required" }.mkString(",\n")}
        |  )
        |  override def updateFormStrongParameters = Seq(
        |${attributePairs.map { case (k, t) => "    \"" + k + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
        |  )
        |
        |}
        |""".stripMargin
    writeIfAbsent(file, code)
    println(s"${file.getAbsolutePath} is created.")
  }

  def generateResourceControllerSpec(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)
    val file = new File(s"src/test/scala/controller/${controllerClassName}Spec.scala")
    val code =
      s"""package controller
        |
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |import model._
        |
        |class ${controllerClassName}Spec extends ScalatraFlatSpec {
        |  addFilter(${controllerClassName}, "/*")
        |
        |  it should "show ${resources}" in {
        |    get("/${resources}") {
        |      status should equal(200)
        |    }
        |    get("/${resources}/") {
        |      status should equal(200)
        |    }
        |    get("/${resources}.json") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |    get("/${resources}.xml") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show a ${resource} in detail" in {
        |    get(s"/${resources}/$${${resource}.id}") {
        |      status should equal(200)
        |    }
        |    get(s"/${resources}/$${${resource}.id}.xml") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |    get(s"/${resources}/$${${resource}.id}.json") {
        |      logger.debug(body)
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
        |    post(s"/${resources}",
        |      ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrfToken" -> "12345") {
        |      post(s"/${resources}",
        |        ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}, "csrfToken" -> "12345") {
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
        |    put(s"/${resources}/$${${resource}.id}",
        |      ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrfToken" -> "12345") {
        |      put(s"/${resources}/$${${resource}.id}", "
        |        ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}, "csrfToken" -> "12345") {
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
    writeIfAbsent(file, code)
  }

  def generateNewView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateEditView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateIndexView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???

  def generateMessages(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val _resources = toClassName(resources)
    val _resource = toClassName(resource)
    val messages =
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
        |${attributePairs.map { case (k, _) => "  " + k + "=\"" + toClassName(k) + "\"" }.mkString("\n")}
        |}
        |""".stripMargin
    val file = new File(s"src/main/resources/messages.conf")
    writeAppending(file, messages)
  }

  def generateSQLs(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val name = toSnakeCase(resources)
    val file = new File(s"src/main/resources/sql/development/${name}_create_table.sql")
    val columns = attributePairs.map {
      case (k, t) =>
        s"  ${toSnakeCase(k)} ${toDBType(t)}" + (if (isOptionClassName(t)) "" else " not null") + ","
    }.mkString("\n")
    val code =
      s"""-- DDL for H2 database
        |create table ${name} (
        |  id identity not null primary key,
        |${columns},
        |  created_at timestamp not null,
        |  updated_at timestamp
        |)
        |""".stripMargin
    writeIfAbsent(file, code)
  }

  private def toSnakeCase(resources: String): String = {
    resources.map(c => if (c.isUpper) "_" + c.toLower else c)
      .mkString
      .replaceFirst("^_", "")
      .replaceFirst("_$", "")
      .replaceFirst("__", "_")
  }

  private def toDBType(t: String): String = {
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

}

