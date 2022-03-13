package skinny.task.generator

import java.io.File
import java.nio.charset.Charset

import org.joda.time._
import org.apache.commons.io.FileUtils
import scalikejdbc.ConnectionPool
import skinny.ParamType

import scala.io.Source

/**
  * Skinny Generator Task.
  */
trait ScaffoldGenerator extends CodeGenerator {

  protected def template: String = "ssp"

  protected def withId: Boolean = true

  protected def withTimestamps: Boolean = true

  protected def primaryKeyName: String = "id"

  protected def primaryKeyType: ParamType = ParamType.Long

  // for reverse-scaffold
  protected def skipDBMigration: Boolean = false

  // for reverse-scaffold
  protected def tableName: Option[String] = None

  protected def snakeCasedPrimaryKeyName: String = toSnakeCase(primaryKeyName)

  protected def customPrimaryKeyName: Option[String] = if (primaryKeyName == "id") None else Option(primaryKeyName)

  protected def useAutoConstruct: Boolean = false

  protected def descendingOrderForIndexPage: Boolean = false

  protected def operationLinksInIndexPageRequired: Boolean = true

  private def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:scaffold members member name:String birthday:Option[LocalDate]" """)
    println(
      """         sbt "task/run generate:scaffold admin.legacy members member name:String birthday:Option[LocalDate]" """
    )
    println("")
  }

  protected def toResourceNameWithNamespace(namespaces: Seq[String], resource: String): String = {
    if (namespaces.isEmpty) resource
    else {
      val modelClassName = toClassName(resource)
      val resourceNameWithNamespace: String = {
        val namespacePart = (namespaces.foldLeft("") {
          case (name, ns) =>
            if (name == "") toCamelCase(ns)
            else name + toFirstCharUpper(toCamelCase(ns))
        })
        if (namespacePart.isEmpty) toCamelCase(modelClassName)
        else namespacePart + toFirstCharUpper(toCamelCase(modelClassName))
      }
      resourceNameWithNamespace
    }
  }

  protected def toFactoryName(namespaces: Seq[String], resource: String): String = {
    "'" + toResourceNameWithNamespace(namespaces, resource)
  }

  def run(args: Seq[String]): Unit = {
    if (args.size < 3) {
      showUsage
      return
    } else if (args(0).contains(":") || args(1).contains(":")) {
      showUsage
      return
    }

    val completedArgs =
      if (args(2).contains(":")) Seq("") ++ args
      else args

    completedArgs match {
      case ns :: rs :: r :: attributes =>
        val (namespaces, resources, resource) = (ns.split('.'), toFirstCharLower(rs), toFirstCharLower(r))
        val errorMessages = attributes
          .flatMap {
            case attr if !attr.contains(":") =>
              Some(s"Invalid parameter (${attr}) found. Scaffold parameter must be delimited with colon(:)")
            case attr =>
              attr.split(":") match {
                case Array(_, paramType) if (!isSupportedParamType(paramType) && !isAssociationTypeName(paramType)) =>
                  Some(s"Invalid type (${paramType}) found. ")
                case _ => None
              }
          }
          .map(_.toString)

        if (!errorMessages.isEmpty) {
          showErrors(errorMessages)
        } else {
          showSkinnyGenerator()

          val generatorArgs: Seq[ScaffoldGeneratorArg] = attributes.flatMap { attribute =>
            attribute.toString.split(":") match {
              case Array(name, typeName, columnName) => Some(ScaffoldGeneratorArg(name, typeName, Some(columnName)))
              case Array(name, typeName)             => Some(ScaffoldGeneratorArg(name, typeName))
              case _                                 => None
            }
          }
          val nameAndTypeNamePairs: Seq[(String, String)] = {
            generatorArgs
              .filterNot(a => isAssociationTypeName(a.typeName))
              .map(a => (a.name, a.typeName))
          }

          if (withId) {
            // Controller
            generateApplicationControllerIfAbsent()
            generateResourceController(namespaces, resources, resource, template, generatorArgs)
            appendToControllers(namespaces, resources)
            generateControllerSpec(namespaces, resources, resource, nameAndTypeNamePairs)
            generateIntegrationTestSpec(namespaces, resources, resource, nameAndTypeNamePairs)

            val name = toResourceNameWithNamespace(namespaces, resource).replaceFirst("'", "")
            appendToFactoriesConf(name, nameAndTypeNamePairs)
          }

          // Model
          val self = this
          val modelGenerator = new ModelGenerator {
            override def connectionPoolName = self.connectionPoolName
            override def primaryKeyName     = self.primaryKeyName
            override def primaryKeyType     = self.primaryKeyType
            override def withId             = self.withId
            override def withTimestamps     = self.withTimestamps
            override def useAutoConstruct   = self.useAutoConstruct

            override def sourceDir       = self.sourceDir
            override def testSourceDir   = self.testSourceDir
            override def resourceDir     = self.resourceDir
            override def testResourceDir = self.testResourceDir
            override def modelPackage    = self.modelPackage
            override def modelPackageDir = self.modelPackageDir
          }
          val nameAndTypeNamePairsForModel = generatorArgs.map(a => (a.name, a.typeName))
          modelGenerator.generate(namespaces,
                                  resource,
                                  tableName.orElse(Some(toSnakeCase(resources))),
                                  nameAndTypeNamePairsForModel)
          modelGenerator.generateSpec(namespaces, resource, nameAndTypeNamePairsForModel)
          val convertedNameAndTypeNamePairs = nameAndTypeNamePairs
            .map { case (name, typeName) => CodeGenerator.convertReservedWord(name) -> typeName }

          if (withId) {
            // Views
            generateFormView(namespaces, resources, resource, convertedNameAndTypeNamePairs)
            generateNewView(namespaces, resources, resource, convertedNameAndTypeNamePairs)
            generateEditView(namespaces, resources, resource, convertedNameAndTypeNamePairs)
            generateIndexView(namespaces, resources, resource, convertedNameAndTypeNamePairs)
            generateShowView(namespaces, resources, resource, convertedNameAndTypeNamePairs)

            // messages.conf
            generateMessages(namespaces, resources, resource, convertedNameAndTypeNamePairs)
          }

          // migration SQL
          if (skipDBMigration == false) {
            generateMigrationSQL(resources, resource, generatorArgs, withId)
          }

          println("")

        }
      case _ => showUsage
    }
  }

  // --------------------------
  // Controller
  // --------------------------

  def generateApplicationControllerIfAbsent(): Unit = {
    val file = new File(s"${sourceDir}/${controllerPackageDir}/ApplicationController.scala")
    writeIfAbsent(
      file,
      s"""package ${controllerPackage}
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
      """.stripMargin
    )
  }

  def controllerCode(namespaces: Seq[String],
                     resources: String,
                     resource: String,
                     template: String,
                     args: Seq[ScaffoldGeneratorArg]): String = {
    val namespace             = toNamespace(controllerPackage, namespaces)
    val resourceWithNamespace = toResourceNameWithNamespace(namespaces, resource)
    val messagesResourceNameIfExists =
      if (resourceWithNamespace != resource) "\n  override def messageResourceName = \"" + resourceWithNamespace + "\""
      else ""
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName      = toClassName(resource)

    val primaryKeyNameIfNotId =
      customPrimaryKeyName.map(name => "\n  override def idName = \"" + name + "\"").getOrElse("")
    val primaryKeyTypeIfNotLong = if (primaryKeyType != ParamType.Long) s"WithId[${primaryKeyType}]" else ""
    val filteredArgs            = args.filterNot(arg => isAssociationTypeName(arg.typeName))
    val validations = filteredArgs
      .filterNot { arg =>
        extractTypeIfOptionOrSeq(arg.typeName) == "Boolean"
      } // boolean param doesn't need required validation.
      .flatMap { arg =>
        val required = if (isOptionClassName(arg.typeName)) Nil else Seq("required")
        val varcharLength =
          if (arg.columnName.isDefined && (
                arg.columnName.get.startsWith("varchar") || arg.columnName.get.startsWith("VARCHAR")
              )) {
            arg.columnName.get.replaceAll("[varcharVARCHAR\\(\\)]", "")
          } else "512"
        val validationRules = required ++ (extractTypeIfOptionOrSeq(arg.typeName) match {
          case "Long"      => Seq("numeric", "longValue")
          case "Int"       => Seq("numeric", "intValue")
          case "Short"     => Seq("numeric", "intValue")
          case "Double"    => Seq("doubleValue")
          case "Float"     => Seq("floatValue")
          case "String"    => Seq(s"maxLength(${varcharLength})")
          case "DateTime"  => Seq("dateTimeFormat")
          case "LocalDate" => Seq("dateFormat")
          case "LocalTime" => Seq("timeFormat")
          case _           => Nil
        })
        if (validationRules.isEmpty) None
        else Some("    paramKey(\"" + toSnakeCase(arg.name) + "\") is " + validationRules.mkString(" & "))
      }
      .mkString(",\n")
    val params = filteredArgs.flatMap {
      case arg =>
        extractTypeIfOptionOrSeq(arg.typeName) match {
          case "DateTime"  => Some(s""".withDateTime("${toSnakeCase(arg.name)}")""")
          case "LocalDate" => Some(s""".withDate("${toSnakeCase(arg.name)}")""")
          case "LocalTime" => Some(s""".withTime("${toSnakeCase(arg.name)}")""")
          case _           => None
        }
    }.mkString

    val overrideMethods = {
      if (descendingOrderForIndexPage) {
        s"""  override protected def findResources(pageSize: Int, pageNo: Int): List[${modelClassName}] = {
           |    model.findModelsDesc(pageSize, pageNo)
           |  }
           |
           |""".stripMargin
      } else {
        ""
      }
    }

    val resourceNameLine =
      s"""override def resourceName = "${resource}"${messagesResourceNameIfExists}${primaryKeyNameIfNotId}"""

    s"""package ${namespace}
        |
        |import skinny._
        |import skinny.validator._
        |import _root_.${controllerPackage}._
        |import ${toNamespace(modelPackage, namespaces)}.${modelClassName}
        |
        |class ${controllerClassName} extends SkinnyResource${primaryKeyTypeIfNotLong} with ApplicationController {
        |  protectFromForgery()
        |
        |  override def model = ${modelClassName}
        |  override def resourcesName = "${resources}"
        |  ${resourceNameLine}
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
        |${filteredArgs
         .map { a =>
           "    \"" + toSnakeCase(a.name) + "\" -> ParamType." + extractTypeIfOptionOrSeq(a.typeName)
         }
         .mkString(",\n")}
        |  )
        |
        |  override def updateParams = Params(params)${params}
        |  override def updateForm = validation(updateParams,
        |${validations}
        |  )
        |  override def updateFormStrongParameters = Seq(
        |${filteredArgs
         .map { a =>
           "    \"" + toSnakeCase(a.name) + "\" -> ParamType." + extractTypeIfOptionOrSeq(a.typeName)
         }
         .mkString(",\n")}
        |  )
        |
        |${overrideMethods}}
        |""".stripMargin
  }

  def generateResourceController(namespaces: Seq[String],
                                 resources: String,
                                 resource: String,
                                 template: String,
                                 args: Seq[ScaffoldGeneratorArg]): Unit = {
    val controllerClassName = toClassName(resources) + "Controller"
    val dir                 = toDirectoryPath(controllerPackageDir, namespaces)
    val file                = new File(s"${sourceDir}/${dir}/${controllerClassName}.scala")
    writeIfAbsent(file, controllerCode(namespaces, resources, resource, template, args))
  }

  private def params(space: String, nameAndTypeNamePairs: Seq[(String, String)]) = {
    nameAndTypeNamePairs
      .map { case (k, t) => toSnakeCase(k) -> extractTypeIfOptionOrSeq(t) }
      .map {
        case (key, paramType) =>
          space + "\"" + key + "\" -> " + {
            paramType match {
              case "Long"      => "Long.MaxValue.toString()"
              case "Int"       => "Int.MaxValue.toString()"
              case "Short"     => "Short.MaxValue.toString()"
              case "Double"    => "Double.MaxValue.toString()"
              case "Float"     => "Float.MaxValue.toString()"
              case "Byte"      => "Byte.MaxValue.toString()"
              case "Boolean"   => "\"true\""
              case "DateTime"  => "skinny.util.DateTimeUtil.toString(new DateTime())"
              case "LocalDate" => "skinny.util.DateTimeUtil.toString(new LocalDate())"
              case "LocalTime" => "skinny.util.DateTimeUtil.toString(new LocalTime())"
              case _           => "\"dummy\""
            }
          }
      }
      .mkString(",\n")
  }

  def controllerSpecCode(namespaces: Seq[String],
                         resources: String,
                         resource: String,
                         nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val namespace           = toNamespace(controllerPackage, namespaces)
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName      = toClassName(resource)
    val factoryNamePart: String = {
      val name = toResourceNameWithNamespace(namespaces, resource)
      if (name != resource) ", \"" + name + "\"" else ""
    }

    val viewTemplatesPath = s"${toResourcesBasePath(namespaces)}/${resources}"
    val resourcesInLabel  = toSplitName(resources)
    val resourceInLabel   = toSplitName(resource)
    val newResourceName   = s"new${toClassName(resource)}"

    s"""package ${namespace}
      |
      |import org.scalatest.funspec.AnyFunSpec
      |import org.scalatest.matchers.should.Matchers
      |import org.scalatest.BeforeAndAfterAll
      |import skinny._
      |import skinny.test._
      |import org.joda.time._
      |import ${toNamespace(modelPackage, namespaces)}._
      |
      |// NOTICE before/after filters won't be executed by default
      |class ${controllerClassName}Spec extends AnyFunSpec with Matchers with BeforeAndAfterAll with DBSettings {
      |
      |  override def afterAll(): Unit = {
      |    super.afterAll()
      |    ${modelClassName}.deleteAll()
      |  }
      |
      |  def createMockController = new ${controllerClassName} with MockController
      |  def ${newResourceName} = FactoryGirl(${modelClassName}${factoryNamePart}).create()
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
      |        controller.getFromRequestScope[${toClassName(resource)}]("item") should equal(Some(${resource}))
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
      |${params("          ", nameAndTypeNamePairs)})
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
      |${params("        ", nameAndTypeNamePairs)})
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

  def generateControllerSpec(namespaces: Seq[String],
                             resources: String,
                             resource: String,
                             nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val controllerClassName = toClassName(resources) + "Controller"
    val dir                 = toDirectoryPath(controllerPackageDir, namespaces)
    val file                = new File(s"${testSourceDir}/${dir}/${controllerClassName}Spec.scala")
    writeIfAbsent(file, controllerSpecCode(namespaces, resources, resource, nameAndTypeNamePairs))
  }

  def integrationSpecCode(namespaces: Seq[String],
                          resources: String,
                          resource: String,
                          nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val namespace           = toNamespace("integrationtest", namespaces)
    val controllerClassName = toClassName(resources) + "Controller"
    val controllerName      = toControllerName(namespaces, resources)
    val modelClassName      = toClassName(resource)
    val factoryNamePart: String = {
      val name = toResourceNameWithNamespace(namespaces, resource)
      if (name != resource) ", \"" + name + "\"" else ""
    }

    val resourcesInLabel = toSplitName(resources)
    val resourceInLabel  = toSplitName(resource)
    val newResourceName  = s"new${toClassName(resource)}"
    val resourceBaseUrl  = s"${toResourcesBasePath(namespaces)}/${toSnakeCase(resources)}"

    s"""package ${namespace}
        |
        |import org.scalatest._
        |import skinny._
        |import skinny.test._
        |import org.joda.time._
        |import _root_.${controllerPackage}.Controllers
        |import ${toNamespace(modelPackage, namespaces)}._
        |
        |class ${controllerClassName}_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
        |  addFilter(Controllers.${controllerName}, "/*")
        |
        |  override def afterAll(): Unit = {
        |    super.afterAll()
        |    ${modelClassName}.deleteAll()
        |  }
        |
        |  def ${newResourceName} = FactoryGirl(${modelClassName}${factoryNamePart}).create()
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
        |${params("      ", nameAndTypeNamePairs)}) {
        |      logBodyUnless(403)
        |      status should equal(403)
        |    }
        |
        |    withSession("csrf-token" -> "valid_token") {
        |      post(s"${resourceBaseUrl}",
        |${params("        ", nameAndTypeNamePairs)},
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
        |${params("      ", nameAndTypeNamePairs)}) {
        |      logBodyUnless(403)
        |      status should equal(403)
        |    }
        |
        |    withSession("csrf-token" -> "valid_token") {
        |      put(s"${resourceBaseUrl}/$${${newResourceName}.${primaryKeyName}}",
        |${params("        ", nameAndTypeNamePairs)},
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

  def generateIntegrationTestSpec(namespaces: Seq[String],
                                  resources: String,
                                  resource: String,
                                  nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val controllerClassName = toClassName(resources) + "Controller"
    val dir                 = toDirectoryPath("integrationtest", namespaces)
    val file                = new File(s"${testSourceDir}/${dir}/${controllerClassName}_IntegrationTestSpec.scala")
    writeIfAbsent(file, integrationSpecCode(namespaces, resources, resource, nameAndTypeNamePairs))
  }

  // --------------------------
  // controller.Controllers.scala
  // --------------------------

  // CodeGenerator#appendToControllers

  // --------------------------
  // factories.conf
  // --------------------------

  def appendToFactoriesConf(factoryName: String, nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val file = new File(s"${testResourceDir}/factories.conf")

    val params = nameAndTypeNamePairs
      .map { case (k, t) => k -> extractTypeIfOptionOrSeq(t) }
      .map {
        case (k, "Long")      => "  " + k + "=\"" + Long.MaxValue.toString() + "\""
        case (k, "Int")       => "  " + k + "=\"" + Int.MaxValue.toString() + "\""
        case (k, "Short")     => "  " + k + "=\"" + Short.MaxValue.toString() + "\""
        case (k, "Double")    => "  " + k + "=\"" + Double.MaxValue.toString() + "\""
        case (k, "Float")     => "  " + k + "=\"" + Float.MaxValue.toString() + "\""
        case (k, "Byte")      => "  " + k + "=\"" + Byte.MaxValue.toString() + "\""
        case (k, "Boolean")   => "  " + k + "=true"
        case (k, "LocalDate") => "  " + k + "=\"" + new LocalDate().toString() + "\""
        case (k, "LocalTime") => "  " + k + "=\"" + new LocalTime().toString() + "\""
        case (k, "DateTime")  => "  " + k + "=\"" + new DateTime().toString() + "\""
        case (k, _)           => "  " + k + "=\"Something New\""
      }
      .mkString("\n")

    val code =
      s"""${factoryName} {
          |${params}
          |}
          |""".stripMargin

    val currentCode = if (file.exists()) Source.fromFile(file).mkString else ""
    if (currentCode.contains(s"${factoryName} {") == false) {
      writeAppending(file, code)
    }
  }

  // --------------------------
  // messages.conf
  // --------------------------

  def messagesConfCode(messageResource: String,
                       resources: String,
                       resource: String,
                       nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val _resources = toCapitalizedSplitName(resources)
    val _resource  = toCapitalizedSplitName(resource)

    s"""
        |${messageResource} {
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
        |${nameAndTypeNamePairs
         .map { case (k, _) => "  " + k + "=\"" + toCapitalizedSplitName(k) + "\"" }
         .mkString("\n")}
        |}
        |""".stripMargin
  }

  def generateMessages(namespaces: Seq[String],
                       resources: String,
                       resource: String,
                       nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val file            = new File(s"${resourceDir}/messages.conf")
    val messageResource = toResourceNameWithNamespace(namespaces, resource)

    val currentCode = if (file.exists()) Source.fromFile(file).mkString else ""
    if (currentCode.contains(s"${messageResource} {") == false) {
      writeAppending(file, messagesConfCode(messageResource, resources, resource, nameAndTypeNamePairs))
    }
  }

  // --------------------------
  // Flyway migration SQL
  // --------------------------

  def migrationSQL(resources: String,
                   resource: String,
                   generatorArgs: Seq[ScaffoldGeneratorArg],
                   withId: Boolean = true): String = {
    val name = tableName.getOrElse(toSnakeCase(resources))
    val columns = generatorArgs
      .map { a =>
        s"  ${toSnakeCase(a.name)} ${a.columnName.getOrElse(toDBType(a.typeName))}" +
        (if (isOptionClassName(a.typeName)) "" else " not null")
      }
      .mkString(",\n")
    val timestamps = if (withTimestamps) {
      s""",
      |  created_at timestamp not null,
      |  updated_at timestamp not null""".stripMargin
    } else ""

    if (withId) {
      s"""-- For H2 Database
          |create table ${name} (
          |  ${toSnakeCase(primaryKeyName)} bigserial not null primary key,
          |${columns}${timestamps}
          |)
          |""".stripMargin
    } else {
      s"""-- For H2 Database : Please add suitable restrictions if needed.
          |create table ${name} (
          |${columns}${timestamps}
          |)
          |""".stripMargin
    }
  }

  def generateMigrationSQL(resources: String,
                           resource: String,
                           generatorArgs: Seq[ScaffoldGeneratorArg],
                           withId: Boolean): Unit = {
    val version = DateTime.now.toString("yyyyMMddHHmmss")
    val file: File = {
      val filepath = {
        val filename: String = s"V${version}__Create_${resources}_table.sql"
        if (connectionPoolName != ConnectionPool.DEFAULT_NAME) {
          val dbName: String = {
            if (connectionPoolName.isInstanceOf[Symbol]) connectionPoolName.asInstanceOf[Symbol].name
            else connectionPoolName.toString
          }
          s"${resourceDir}/db/${dbName}/migration/${filename}"
        } else {
          s"${resourceDir}/db/migration/${filename}"
        }
      }
      new File(filepath)
    }

    val sql = migrationSQL(resources, resource, generatorArgs, withId)
    writeIfAbsent(file, sql)
  }

  // --------------------------
  // Views
  // --------------------------

  def formHtmlCode(namespaces: Seq[String],
                   resources: String,
                   resource: String,
                   nameAndTypeNamePairs: Seq[(String, String)]): String
  def newHtmlCode(namespaces: Seq[String],
                  resources: String,
                  resource: String,
                  nameAndTypeNamePairs: Seq[(String, String)]): String
  def editHtmlCode(namespaces: Seq[String],
                   resources: String,
                   resource: String,
                   nameAndTypeNamePairs: Seq[(String, String)]): String
  def indexHtmlCode(namespaces: Seq[String],
                    resources: String,
                    resource: String,
                    nameAndTypeNamePairs: Seq[(String, String)]): String
  def showHtmlCode(namespaces: Seq[String],
                   resources: String,
                   resource: String,
                   nameAndTypeNamePairs: Seq[(String, String)]): String

  def generateFormView(namespaces: Seq[String],
                       resources: String,
                       resource: String,
                       nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val dir     = toDirectoryPath("views", namespaces)
    val viewDir = s"${webInfDir}/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/_form.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file,
                      formHtmlCode(namespaces, resources, resource, nameAndTypeNamePairs),
                      Charset.defaultCharset())
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateNewView(namespaces: Seq[String],
                      resources: String,
                      resource: String,
                      nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val dir     = toDirectoryPath("views", namespaces)
    val viewDir = s"${webInfDir}/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/new.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file,
                      newHtmlCode(namespaces, resources, resource, nameAndTypeNamePairs),
                      Charset.defaultCharset())
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateEditView(namespaces: Seq[String],
                       resources: String,
                       resource: String,
                       nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val dir     = toDirectoryPath("views", namespaces)
    val viewDir = s"${webInfDir}/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/edit.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file,
                      editHtmlCode(namespaces, resources, resource, nameAndTypeNamePairs),
                      Charset.defaultCharset())
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateIndexView(namespaces: Seq[String],
                        resources: String,
                        resource: String,
                        nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val dir     = toDirectoryPath("views", namespaces)
    val viewDir = s"${webInfDir}/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/index.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file,
                      indexHtmlCode(namespaces, resources, resource, nameAndTypeNamePairs),
                      Charset.defaultCharset())
      println("  \"" + file.getPath + "\" created.")
    }
  }

  def generateShowView(namespaces: Seq[String],
                       resources: String,
                       resource: String,
                       nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val dir     = toDirectoryPath("views", namespaces)
    val viewDir = s"${webInfDir}/${dir}/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val file = new File(s"${viewDir}/show.html.${template}")
    if (file.exists()) {
      println("  \"" + file.getPath + "\" skipped.")
    } else {
      FileUtils.write(file,
                      showHtmlCode(namespaces, resources, resource, nameAndTypeNamePairs),
                      Charset.defaultCharset())
      println("  \"" + file.getPath + "\" created.")
    }
  }
}
