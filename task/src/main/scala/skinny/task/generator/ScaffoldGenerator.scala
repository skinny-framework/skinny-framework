package skinny.task.generator

import java.io.File
import scala.io.Source
import org.joda.time._
import org.apache.commons.io.FileUtils
import skinny.ParamType

/**
 * Skinny Generator Task.
 */
trait ScaffoldGenerator extends CodeGenerator {

  protected def template: String = "ssp"

  protected def withTimestamps: Boolean = true

  protected def primaryKeyName: String = "id"

  protected def primaryKeyType: ParamType = ParamType.Long

  // for reverse-scaffold
  protected def skipDBMigration: Boolean = false

  // for reverse-scaffold
  protected def tableName: Option[String] = None

  protected def snakeCasedPrimaryKeyName: String = toSnakeCase(primaryKeyName)

  protected def customPrimaryKeyName: Option[String] = if (primaryKeyName == "id") None else Option(primaryKeyName)

  private def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:scaffold members member name:String birthday:Option[LocalDate]" """)
    println("""         sbt "task/run generate:scaffold admin.legacy members member name:String birthday:Option[LocalDate]" """)
    println("")
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
    "BigDecimal",
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
    "Option[BigDecimal]",
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
      case "BigDecimal" => "numeric"
      case "DateTime" => "timestamp"
      case "LocalDate" => "date"
      case "LocalTime" => "time"
      case "Boolean" => "boolean"
      case "Double" => "double"
      case "Float" => "float"
      case _ => "other"
    }
  }

  def run(args: Seq[String]) {
    if (args.size < 3) {
      showUsage
      return
    } else if (args(0).contains(":") || args(1).contains(":")) {
      showUsage
      return
    }

    val completedArgs = if (args(2).contains(":")) Seq("") ++ args
    else args

    completedArgs match {
      case ns :: rs :: r :: attributes =>
        val (namespaces, resources, resource) = (ns.split('.'), toFirstCharLower(rs), toFirstCharLower(r))
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
          generateResourceController(namespaces, resources, resource, template, generatorArgs)
          appendToControllers(namespaces, resources)
          generateControllerSpec(namespaces, resources, resource, attributePairs)
          generateIntegrationTestSpec(namespaces, resources, resource, attributePairs)
          appendToFactoriesConf(resource, attributePairs)

          // Model
          val self = this
          val modelGenerator = new ModelGenerator {
            override def primaryKeyName = self.primaryKeyName
            override def primaryKeyType = self.primaryKeyType
            override def withTimestamps = self.withTimestamps
          }
          modelGenerator.generate(namespaces, resource, tableName.orElse(Some(toSnakeCase(resources))), attributePairs)
          modelGenerator.generateSpec(namespaces, resource, attributePairs)

          // Views
          generateFormView(namespaces, resources, resource, attributePairs)
          generateNewView(namespaces, resources, resource, attributePairs)
          generateEditView(namespaces, resources, resource, attributePairs)
          generateIndexView(namespaces, resources, resource, attributePairs)
          generateShowView(namespaces, resources, resource, attributePairs)

          // messages.conf
          generateMessages(resources, resource, attributePairs)

          // migration SQL
          generateMigrationSQL(resources, resource, generatorArgs, skipDBMigration)

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
        |  // override def defaultLocale = Some(new java.util.Locale("ja"))
        |
        |}
      """.stripMargin)
  }

  def controllerCode(namespaces: Seq[String], resources: String, resource: String, template: String, args: Seq[ScaffoldGeneratorArg]): String = {
    val namespace = toNamespace("controller", namespaces)
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)

    val primaryKeyNameIfNotId = customPrimaryKeyName.map(name => "\n  override def idName = \"" + name + "\"").getOrElse("")
    val primaryKeyTypeIfNotLong = if (primaryKeyType != ParamType.Long) s"WithId[${primaryKeyType}]" else ""
    val validations = args
      .filterNot { case arg => toParamType(arg.typeName) == "Boolean" } // boolean param doesn't need required validation.
      .flatMap { arg =>
        val required = if (isOptionClassName(arg.typeName)) Nil else Seq("required")
        val varcharLength = if (arg.columnName.isDefined && (
          arg.columnName.get.startsWith("varchar") || arg.columnName.get.startsWith("VARCHAR"))) {
          arg.columnName.get.replaceAll("[varcharVARCHAR\\(\\)]", "")
        } else "512"
        val validationRules = required ++ (toParamType(arg.typeName) match {
          case "Long" => Seq("numeric", "longValue")
          case "Int" => Seq("numeric", "intValue")
          case "Short" => Seq("numeric", "intValue")
          case "Double" => Seq("doubleValue")
          case "Float" => Seq("floatValue")
          case "String" => Seq(s"maxLength(${varcharLength})")
          case "DateTime" => Seq("dateTimeFormat")
          case "LocalDate" => Seq("dateFormat")
          case "LocalTime" => Seq("timeFormat")
          case _ => Nil
        })
        if (validationRules.isEmpty) None
        else Some("    paramKey(\"" + toSnakeCase(arg.name) + "\") is " + validationRules.mkString(" & "))
      }
      .mkString(",\n")
    val params = args.flatMap {
      case arg =>
        toParamType(arg.typeName) match {
          case "DateTime" => Some(s""".withDateTime("${toSnakeCase(arg.name)}")""")
          case "LocalDate" => Some(s""".withDate("${toSnakeCase(arg.name)}")""")
          case "LocalTime" => Some(s""".withTime("${toSnakeCase(arg.name)}")""")
          case _ => None
        }
    }.mkString

    s"""package ${namespace}
        |
        |import skinny._
        |import skinny.validator._
        |import _root_.controller._
        |import ${toNamespace("model", namespaces)}.${modelClassName}
        |
        |class ${controllerClassName} extends SkinnyResource${primaryKeyTypeIfNotLong} with ApplicationController {
        |  protectFromForgery()
        |
        |  override def model = ${modelClassName}
        |  override def resourcesName = "${resources}"
        |  override def resourceName = "${resource}"${primaryKeyNameIfNotId}
        |
        |  override def resourcesBasePath = s"${toResourcesBasePath(namespaces)}/$${toSnakeCase(resourcesName)}"
        |  override def useSnakeCasedParamKeys = true
        |
        |  override def viewsDirectoryPath = s"${toResourcesBasePath(namespaces)}/$${resourcesName}"
        |
        |  override def createParams = Params(params)${params}
        |  override def createForm = validation(createParams,
        |${validations}
        |  )
        |  override def createFormStrongParameters = Seq(
        |${args.map { a => "    \"" + toSnakeCase(a.name) + "\" -> ParamType." + toParamType(a.typeName) }.mkString(",\n")}
        |  )
        |
        |  override def updateParams = Params(params)${params}
        |  override def updateForm = validation(updateParams,
        |${validations}
        |  )
        |  override def updateFormStrongParameters = Seq(
        |${args.map { a => "    \"" + toSnakeCase(a.name) + "\" -> ParamType." + toParamType(a.typeName) }.mkString(",\n")}
        |  )
        |
        |}
        |""".stripMargin
  }

  def generateResourceController(namespaces: Seq[String], resources: String, resource: String, template: String, args: Seq[ScaffoldGeneratorArg]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val dir = toDirectoryPath("controller", namespaces)
    val file = new File(s"src/main/scala/${dir}/${controllerClassName}.scala")
    writeIfAbsent(file, controllerCode(namespaces, resources, resource, template, args))
  }

  private def params(space: String, attributePairs: Seq[(String, String)]) = {
    attributePairs.map { case (k, t) => toSnakeCase(k) -> toParamType(t) }.map {
      case (key, paramType) =>
        space + "\"" + key + "\" -> " + {
          paramType match {
            case "Long" => "Long.MaxValue.toString()"
            case "Int" => "Int.MaxValue.toString()"
            case "Short" => "Short.MaxValue.toString()"
            case "Double" => "Double.MaxValue.toString()"
            case "Float" => "Float.MaxValue.toString()"
            case "Byte" => "Byte.MaxValue.toString()"
            case "Boolean" => "\"true\""
            case "DateTime" => "skinny.util.DateTimeUtil.toString(new DateTime())"
            case "LocalDate" => "skinny.util.DateTimeUtil.toString(new LocalDate())"
            case "LocalTime" => "skinny.util.DateTimeUtil.toString(new LocalTime())"
            case _ => "\"dummy\""
          }
        }
    }.mkString(",\n")
  }

  def controllerSpecCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val namespace = toNamespace("controller", namespaces)
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)

    val viewTemplatesPath = s"${toResourcesBasePath(namespaces)}/${resources}"
    val resourcesInLabel = toSplitName(resources)
    val resourceInLabel = toSplitName(resource)
    val newResourceName = s"new${toClassName(resource)}"

    s"""package ${namespace}
      |
      |import org.scalatest.FunSpec
      |import org.scalatest.matchers.ShouldMatchers
      |import skinny._
      |import skinny.test._
      |import org.joda.time._
      |import ${toNamespace("model", namespaces)}._
      |
      |// NOTICE before/after filters won't be executed by default
      |class ${controllerClassName}Spec extends FunSpec with ShouldMatchers with DBSettings {
      |
      |  def createMockController = new ${controllerClassName} with MockController
      |  def ${newResourceName} = FactoryGirl(${modelClassName}).create()
      |
      |  describe("${controllerClassName}") {
      |
      |    describe("shows ${resourcesInLabel}") {
      |      it("shows HTML response") {
      |        val controller = createMockController
      |        controller.showResources()
      |        controller.status should equal(200)
      |        controller.renderCall.map(_.path) should equal(Some("${viewTemplatesPath}/index"))
      |        controller.contentType should equal("text/html; charset=utf-8")
      |      }
      |
      |      it("shows JSON response") {
      |        implicit val format = Format.JSON
      |        val controller = createMockController
      |        controller.showResources()
      |        controller.status should equal(200)
      |        controller.renderCall.map(_.path) should equal(Some("${viewTemplatesPath}/index"))
      |        controller.contentType should equal("application/json; charset=utf-8")
      |      }
      |    }
      |
      |    describe("shows a ${resourceInLabel}") {
      |      it("shows HTML response") {
      |        val ${resource} = ${newResourceName}
      |        val controller = createMockController
      |        controller.showResource(${resource}.${primaryKeyName})
      |        controller.status should equal(200)
      |        controller.requestScope[${toClassName(resource)}]("item") should equal(Some(${resource}))
      |        controller.renderCall.map(_.path) should equal(Some("${viewTemplatesPath}/show"))
      |      }
      |    }
      |
      |    describe("shows new resource input form") {
      |      it("shows HTML response") {
      |        val controller = createMockController
      |        controller.newResource()
      |        controller.status should equal(200)
      |        controller.renderCall.map(_.path) should equal(Some("${viewTemplatesPath}/new"))
      |      }
      |    }
      |
      |    describe("creates a ${resourceInLabel}") {
      |      it("succeeds with valid parameters") {
      |        val controller = createMockController
      |        controller.prepareParams(
      |${params("          ", attributePairs)})
      |        controller.createResource()
      |        controller.status should equal(200)
      |      }
      |
      |      it("fails with invalid parameters") {
      |        val controller = createMockController
      |        controller.prepareParams() // no parameters
      |        controller.createResource()
      |        controller.status should equal(400)
      |        controller.errorMessages.size should be >(0)
      |      }
      |    }
      |
      |    it("shows a resource edit input form") {
      |      val ${resource} = ${newResourceName}
      |      val controller = createMockController
      |      controller.editResource(${resource}.${primaryKeyName})
      |      controller.status should equal(200)
      |        controller.renderCall.map(_.path) should equal(Some("${viewTemplatesPath}/edit"))
      |    }
      |
      |    it("updates a ${resourceInLabel}") {
      |      val ${resource} = ${newResourceName}
      |      val controller = createMockController
      |      controller.prepareParams(
      |${params("        ", attributePairs)})
      |      controller.updateResource(${resource}.${primaryKeyName})
      |      controller.status should equal(200)
      |    }
      |
      |    it("destroys a ${resourceInLabel}") {
      |      val ${resource} = ${newResourceName}
      |      val controller = createMockController
      |      controller.destroyResource(${resource}.${primaryKeyName})
      |      controller.status should equal(200)
      |    }
      |
      |  }
      |
      |}
      |""".stripMargin
  }

  def generateControllerSpec(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val dir = toDirectoryPath("controller", namespaces)
    val file = new File(s"src/test/scala/${dir}/${controllerClassName}Spec.scala")
    writeIfAbsent(file, controllerSpecCode(namespaces, resources, resource, attributePairs))
  }

  def toControllerName(namespaces: Seq[String], resources: String): String = {
    if (namespaces.filterNot(_.isEmpty).isEmpty) toCamelCase(resources)
    else namespaces.head + namespaces.tail.map { n => n.head.toUpper + n.tail }.mkString + toClassName(resources)
  }

  def integrationSpecCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val namespace = toNamespace("integrationtest", namespaces)
    val controllerClassName = toClassName(resources) + "Controller"
    val controllerName = toControllerName(namespaces, resources)
    val modelClassName = toClassName(resource)

    val resourcesInLabel = toSplitName(resources)
    val resourceInLabel = toSplitName(resource)
    val newResourceName = s"new${toClassName(resource)}"
    val resourceBaseUrl = s"${toResourcesBasePath(namespaces)}/${toSnakeCase(resources)}"

    s"""package ${namespace}
        |
        |import org.scalatra.test.scalatest._
        |import skinny._
        |import skinny.test._
        |import org.joda.time._
        |import _root_.controller.Controllers
        |import ${toNamespace("model", namespaces)}._
        |
        |class ${controllerClassName}_IntegrationTestSpec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings {
        |  addFilter(Controllers.${controllerName}, "/*")
        |
        |  def ${newResourceName} = FactoryGirl(${modelClassName}).create()
        |
        |  it should "show ${resourcesInLabel}" in {
        |    get("${resourceBaseUrl}") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |    get("${resourceBaseUrl}/") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |    get("${resourceBaseUrl}.json") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |    get("${resourceBaseUrl}.xml") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show a ${resourceInLabel} in detail" in {
        |    get(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |    get(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}.xml") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |    get(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}.json") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show new entry form" in {
        |    get(s"${resourceBaseUrl}/new") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "create a ${resourceInLabel}" in {
        |    post(s"${resourceBaseUrl}",
        |${params("      ", attributePairs)}) {
        |      logBodyUnless(403)
        |      status should equal(403)
        |    }
        |
        |    withSession("csrf-token" -> "valid_token") {
        |      post(s"${resourceBaseUrl}",
        |${params("        ", attributePairs)},
        |        "csrf-token" -> "valid_token") {
        |        logBodyUnless(302)
        |        status should equal(302)
        |        val id = header("Location").split("/").last${if (primaryKeyType == ParamType.Long) ".toLong" else ""}
        |        ${modelClassName}.findById(id).isDefined should equal(true)
        |      }
        |    }
        |  }
        |
        |  it should "show the edit form" in {
        |    get(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}/edit") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "update a ${resourceInLabel}" in {
        |    put(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}",
        |${params("      ", attributePairs)}) {
        |      logBodyUnless(403)
        |      status should equal(403)
        |    }
        |
        |    withSession("csrf-token" -> "valid_token") {
        |      put(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}",
        |${params("        ", attributePairs)},
        |        "csrf-token" -> "valid_token") {
        |        logBodyUnless(302)
        |        status should equal(302)
        |      }
        |    }
        |  }
        |
        |  it should "delete a ${resourceInLabel}" in {
        |    delete(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}") {
        |      logBodyUnless(403)
        |      status should equal(403)
        |    }
        |    withSession("csrf-token" -> "valid_token") {
        |      delete(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}?csrf-token=valid_token") {
        |        logBodyUnless(200)
        |        status should equal(200)
        |      }
        |    }
        |  }
        |
        |}
        |""".stripMargin
  }

  def generateIntegrationTestSpec(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val dir = toDirectoryPath("integrationtest", namespaces)
    val file = new File(s"src/test/scala/${dir}/${controllerClassName}_IntegrationTestSpec.scala")
    writeIfAbsent(file, integrationSpecCode(namespaces, resources, resource, attributePairs))
  }

  // --------------------------
  // controller.Controllers.scala
  // --------------------------

  def appendToControllers(namespaces: Seq[String], resources: String) {
    val controllerName = toControllerName(namespaces, resources)
    val controllerClassName = toNamespace("_root_.controller", namespaces) + "." + toControllerClassName(resources)
    val newMountCode =
      s"""def mount(ctx: ServletContext): Unit = {
        |    ${controllerName}.mount(ctx)""".stripMargin
    val newControllerDefCode = {
      s"""  object ${controllerName} extends ${controllerClassName} {
        |  }
        |
        |}
        |""".stripMargin
    }

    val file = new File("src/main/scala/controller/Controllers.scala")
    if (file.exists()) {
      val code = Source.fromFile(file).mkString
        .replaceFirst("(def\\s+mount\\s*\\(ctx:\\s+ServletContext\\):\\s*Unit\\s*=\\s*\\{)", newMountCode)
        .replaceFirst("(}[\\s\\r\\n]+)$", newControllerDefCode)
      forceWrite(file, code)
    } else {
      val fullNewCode =
        s"""package controller
          |
          |import _root_.controller._
          |import skinny._
          |import skinny.controller.AssetsController
          |
          |object Controllers {
          |
          |  ${newMountCode}
          |    AssetsController.mount(ctx)
          |  }
          |
          |${newControllerDefCode}
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
        |  ${primaryKeyName}="ID"
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
    val name = tableName.getOrElse(toSnakeCase(resources))
    val columns = generatorArgs.map { a =>
      s"  ${toSnakeCase(a.name)} ${a.columnName.getOrElse(toDBType(a.typeName))}" +
        (if (isOptionClassName(a.typeName)) "" else " not null")
    }.mkString(",\n")
    val timestamps = if (withTimestamps) {
      s""",
      |  created_at timestamp not null,
      |  updated_at timestamp not null""".stripMargin
    } else ""

    s"""-- For H2 Database
        |create table ${name} (
        |  ${toSnakeCase(primaryKeyName)} bigserial not null primary key,
        |${columns}${timestamps}
        |)
        |""".stripMargin
  }

  def generateMigrationSQL(resources: String, resource: String, generatorArgs: Seq[ScaffoldGeneratorArg], skip: Boolean) {
    val version = DateTime.now.toString("yyyyMMddHHmmss")
    val file = new File(s"src/main/resources/db/migration/V${version}__Create_${resources}_table.sql")
    val sql = migrationSQL(resources, resource, generatorArgs)
    writeIfAbsent(file, if (skip) s"/*\n${sql}\n*/" else sql)
  }

  // --------------------------
  // Views
  // --------------------------

  def formHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def newHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def editHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def indexHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String
  def showHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String

  def generateFormView(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val dir = toDirectoryPath("views", namespaces)
    val viewDir = s"src/main/webapp/WEB-INF/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/_form.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, formHtmlCode(namespaces, resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateNewView(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val dir = toDirectoryPath("views", namespaces)
    val viewDir = s"src/main/webapp/WEB-INF/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/new.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, newHtmlCode(namespaces, resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateEditView(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val dir = toDirectoryPath("views", namespaces)
    val viewDir = s"src/main/webapp/WEB-INF/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/edit.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, editHtmlCode(namespaces, resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateIndexView(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val dir = toDirectoryPath("views", namespaces)
    val viewDir = s"src/main/webapp/WEB-INF/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/index.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, indexHtmlCode(namespaces, resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateShowView(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val dir = toDirectoryPath("views", namespaces)
    val viewDir = s"src/main/webapp/WEB-INF/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/show.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file, showHtmlCode(namespaces, resources, resource, attributePairs))
      println("  \"" + file.getPath + "\" created.")
    }
  }
}
