package skinny.task.generator

import java.io.File
import scala.io.Source
import org.joda.time._
import org.apache.commons.io.FileUtils

/**
 * Skinny Generator Task.
 */
trait ScaffoldGenerator extends CodeGenerator {

  protected def template: String = "ssp"

  protected def withTimestamps: Boolean = true

  private def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:scaffold members member name:String birthday:Option[LocalDate]" """)
    println("")
  }

  private def showErrors(messages: Seq[String]) = {
    showSkinnyGenerator()
    println("""  Command failed!""")
    println("")
    println(messages.mkString("  Error: ", "\n", "\n"))
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
        val errorMessages = attributes.flatMap {
          case attr if !attr.contains(":") => Some(s"Invalid parameter (${attr}) found. Scaffold parameter must be delimited with colon(:)")
          case attr => attr.split(":") match {
            case Array(_, paramType) if !paramTypes.contains(paramType) => Some(s"Invalid type (${paramType}) found. ")
            case _ => None
          }
        }.map(_.toString)

        if (!errorMessages.isEmpty) {
          showErrors(errorMessages)
        } else {
          showSkinnyGenerator()

          val generatorArgs: Seq[ScaffoldGeneratorArg] = attributes.flatMap { attribute =>
            attribute.toString.split(":") match {
              case Array(name, typeName, columnName) => Some(ScaffoldGeneratorArg(name, typeName, Some(columnName)))
              case Array(name, typeName) => Some(ScaffoldGeneratorArg(name, typeName))
              case _ => None
            }
          }
          val attributePairs: Seq[(String, String)] = generatorArgs.map(a => (a.name, a.typeName))

          // Controller
          generateApplicationControllerIfAbsent()
          generateResourceController(resources, resource, template, attributePairs)
          appendToScalatraBootstrap(resources)
          generateResourceControllerSpec(resources, resource, attributePairs)
          appendToFactoriesConf(resource, attributePairs)
          // Model
          val self = this
          val modelGenerator = new ModelGenerator { override def withTimestamps = self.withTimestamps }
          modelGenerator.generate(resource, Some(toSnakeCase(resources)), attributePairs)
          modelGenerator.generateSpec(resource, attributePairs)
          // Views
          generateFormView(resources, resource, attributePairs)
          generateNewView(resources, resource, attributePairs)
          generateEditView(resources, resource, attributePairs)
          generateIndexView(resources, resource, attributePairs)
          generateShowView(resources, resource, attributePairs)
          // messages.conf
          generateMessages(resources, resource, attributePairs)
          // migration SQL
          generateMigrationSQL(resources, resource, generatorArgs)

          println("")

        }
      case _ => showUsage
    }
  }

  // --------------------------
  // Controller
  // --------------------------

  def generateApplicationControllerIfAbsent() {
    val file = new File(s"src/main/scala/controller/ApplicationController.scala")
    writeIfAbsent(file,
      """package controller
        |
        |import skinny._
        |import skinny.filter._
        |
        |/**
        | * The base controller for this Skinny application.
        | *
        | * see also "http://skinny-framework.org/documentation/controller-and-routes.html"
        | */
        |trait ApplicationController extends SkinnyController
        |  // with TxPerRequestFilter
        |  // with SkinnySessionFilter
        |  with ErrorPageFilter {
        |
        |}
      """.stripMargin)
  }

  def controllerCode(resources: String, resource: String, template: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)
    val validations = attributePairs
      .filterNot { case (_, t) => toParamType(t) == "Boolean" } // boolean param doesn't need required valdiation.
      .flatMap {
        case (k, t) =>
          val validationRules = (if (isOptionClassName(t)) Nil else Seq("required")) ++ (toParamType(t) match {
            case "Long" => Seq("numeric", "longValue")
            case "Int" => Seq("numeric", "intValue")
            case "Short" => Seq("numeric", "intValue")
            case "Double" => Seq("doubleValue")
            case "Float" => Seq("floatValue")
            case "String" => Seq("maxLength(512)")
            case "DateTime" => Seq("dateTimeFormat")
            case "LocalDate" => Seq("dateFormat")
            case "LocalTime" => Seq("timeFormat")
            case _ => Nil
          })
          if (validationRules.isEmpty) None
          else Some("    paramKey(\"" + toSnakeCase(k) + "\") is " + validationRules.mkString(" & "))
      }
      .mkString(",\n")
    val params = attributePairs.flatMap {
      case (name, t) =>
        toParamType(t) match {
          case "DateTime" => Some(s""".withDateTime("${toSnakeCase(name)}")""")
          case "LocalDate" => Some(s""".withDate("${toSnakeCase(name)}")""")
          case "LocalTime" => Some(s""".withTime("${toSnakeCase(name)}")""")
          case _ => None
        }
    }.mkString

    s"""package controller
        |
        |import skinny._
        |import skinny.validator._
        |import model.${modelClassName}
        |
        |object ${controllerClassName} extends SkinnyResource with ApplicationController {
        |  protectFromForgery()
        |
        |  override def model = ${modelClassName}
        |  override def resourcesName = "${resources}"
        |  override def resourceName = "${resource}"
        |
        |  override def resourcesBasePath = s"/$${toSnakeCase(resourcesName)}"
        |  override def useSnakeCasedParamKeys = true
        |
        |  override def createForm = validation(createParams,
        |${validations}
        |  )
        |  override def createParams = Params(params)${params}
        |  override def createFormStrongParameters = Seq(
        |${attributePairs.map { case (k, t) => "    \"" + toSnakeCase(k) + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
        |  )
        |
        |  override def updateForm = validation(updateParams,
        |${validations}
        |  )
        |  override def updateParams = Params(params)${params}
        |  override def updateFormStrongParameters = Seq(
        |${attributePairs.map { case (k, t) => "    \"" + toSnakeCase(k) + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
        |  )
        |
        |}
        |""".stripMargin
  }

  def generateResourceController(resources: String, resource: String, template: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val file = new File(s"src/main/scala/controller/${controllerClassName}.scala")
    writeIfAbsent(file, controllerCode(resources, resource, template, attributePairs))
  }

  def controllerSpecCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)

    val params = attributePairs.map { case (k, t) => toSnakeCase(k) -> toParamType(t) }.map {
      case (k, "Long") => "\"" + k + "\" -> Long.MaxValue.toString()"
      case (k, "Int") => "\"" + k + "\" -> Int.MaxValue.toString()"
      case (k, "Short") => "\"" + k + "\" -> Short.MaxValue.toString()"
      case (k, "Double") => "\"" + k + "\" -> Double.MaxValue.toString()"
      case (k, "Float") => "\"" + k + "\" -> Float.MaxValue.toString()"
      case (k, "Byte") => "\"" + k + "\" -> Byte.MaxValue.toString()"
      case (k, "Boolean") => "\"" + k + "\" -> \"true\""
      case (k, "DateTime") => "\"" + k + "\" -> new DateTime().toString(\"YYYY-MM-dd hh:mm:ss\")"
      case (k, "LocalDate") => "\"" + k + "\" -> new LocalDate().toString(\"YYYY-MM-dd\")"
      case (k, "LocalTime") => "\"" + k + "\" -> new LocalTime().toString(\"hh:mm:ss\")"
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
        |    get("/${toSnakeCase(resources)}") {
        |      status should equal(200)
        |    }
        |    get("/${toSnakeCase(resources)}/") {
        |      status should equal(200)
        |    }
        |    get("/${toSnakeCase(resources)}.json") {
        |      status should equal(200)
        |    }
        |    get("/${toSnakeCase(resources)}.xml") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show a ${resource} in detail" in {
        |    get(s"/${toSnakeCase(resources)}/$${${resource}.id}") {
        |      status should equal(200)
        |    }
        |    get(s"/${toSnakeCase(resources)}/$${${resource}.id}.xml") {
        |      status should equal(200)
        |    }
        |    get(s"/${toSnakeCase(resources)}/$${${resource}.id}.json") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show new entry form" in {
        |    get(s"/${toSnakeCase(resources)}/new") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "create a ${resource}" in {
        |    post(s"/${toSnakeCase(resources)}", ${params}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrf-token" -> "12345") {
        |      post(s"/${toSnakeCase(resources)}", ${params}, "csrf-token" -> "12345") {
        |        status should equal(302)
        |        val id = header("Location").split("/").last.toLong
        |        ${modelClassName}.findById(id).isDefined should equal(true)
        |      }
        |    }
        |  }
        |
        |  it should "show the edit form" in {
        |    get(s"/${toSnakeCase(resources)}/$${${resource}.id}/edit") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "update a ${resource}" in {
        |    put(s"/${toSnakeCase(resources)}/$${${resource}.id}", ${params}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrf-token" -> "12345") {
        |      put(s"/${toSnakeCase(resources)}/$${${resource}.id}", ${params}, "csrf-token" -> "12345") {
        |        status should equal(302)
        |      }
        |    }
        |  }
        |
        |  it should "delete a ${resource}" in {
        |    val ${resource} = FactoryGirl(${modelClassName}).create()
        |    delete(s"/${toSnakeCase(resources)}/$${${resource}.id}") {
        |      status should equal(403)
        |    }
        |    withSession("csrf-token" -> "aaaaaa") {
        |      delete(s"/${toSnakeCase(resources)}/$${${resource}.id}?csrf-token=aaaaaa") {
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
        |    ${controllerClassName}.mount(ctx)""".stripMargin
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
      case (k, "Long") => "  " + k + "=\"" + Long.MaxValue.toString() + "\""
      case (k, "Int") => "  " + k + "=\"" + Int.MaxValue.toString() + "\""
      case (k, "Short") => "  " + k + "=\"" + Short.MaxValue.toString() + "\""
      case (k, "Double") => "  " + k + "=\"" + Double.MaxValue.toString() + "\""
      case (k, "Float") => "  " + k + "=\"" + Float.MaxValue.toString() + "\""
      case (k, "Byte") => "  " + k + "=\"" + Byte.MaxValue.toString() + "\""
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
    val _resources = toCapitalizedSplitName(resources)
    val _resource = toCapitalizedSplitName(resource)

    s"""
        |${resource} {
        |  flash {
        |    created="The ${toSplitName(resource)} was created."
        |    updated="The ${toSplitName(resource)} was updated."
        |    deleted="The ${toSplitName(resource)} was deleted."
        |  }
        |  list="${_resources}"
        |  detail="${_resource}"
        |  edit="Edit ${_resource}"
        |  new="New ${_resource}"
        |  delete.confirm="Are you sure?"
        |  id="ID"
        |${attributePairs.map { case (k, _) => "  " + k + "=\"" + toCapitalizedSplitName(k) + "\"" }.mkString("\n")}
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

  def migrationSQL(resources: String, resource: String, generatorArgs: Seq[ScaffoldGeneratorArg]): String = {
    val name = toSnakeCase(resources)
    val columns = generatorArgs.map { a =>
      s"  ${toSnakeCase(a.name)} ${a.columnName.getOrElse(toDBType(a.typeName))}" +
        (if (isOptionClassName(a.typeName)) "" else " not null")
    }.mkString(",\n")
    val timestamps = if (withTimestamps) {
      s""",
      |  created_at timestamp not null,
      |  updated_at timestamp""".stripMargin
    } else ""

    s"""-- For H2 Database
        |create table ${name} (
        |  id bigserial not null primary key,
        |${columns}${timestamps}
        |)
        |""".stripMargin
  }

  def generateMigrationSQL(resources: String, resource: String, generatorArgs: Seq[ScaffoldGeneratorArg]) {
    val version = DateTime.now.toString("yyyyMMddHHmmss")
    val file = new File(s"src/main/resources/db/migration/V${version}__Create_${resources}_table.sql")
    writeIfAbsent(file, migrationSQL(resources, resource, generatorArgs))
  }

  // --------------------------
  // Views
  // --------------------------

  def formHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def newHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def editHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def indexHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def showHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String

  def generateFormView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/_form.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, formHtmlCode(resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateNewView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/new.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, newHtmlCode(resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateEditView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/edit.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, editHtmlCode(resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateIndexView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/index.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, indexHtmlCode(resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/show.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, showHtmlCode(resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }
}
