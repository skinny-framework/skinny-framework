package skinny.task.generator

import java.io.File
import scala.io.Source

/**
  * Controller generator.
  */
object ControllerGenerator extends ControllerGenerator

trait ControllerGenerator extends CodeGenerator {

  private[this] def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:controller help" """)
    println("""         sbt "task/run generate:controller admin.legacy help" """)
    println("")
  }

  def run(args: List[String]): Unit = {
    val completedArgs: Seq[String] = if (args.size == 1) {
      if (args.head.contains(".")) {
        val elements = args.head.split("\\.")
        Seq(elements.init.mkString("."), elements.last)
      } else Seq("") ++ args
    } else args

    completedArgs match {
      case namespace :: name :: _ =>
        val namespaces = namespace.split('.')
        showSkinnyGenerator()
        generateApplicationControllerIfAbsent()
        generate(namespaces, name)
        appendToControllers(namespaces, name)
        generateControllerSpec(namespaces, name)
        generateIntegrationSpec(namespaces, name)
        println("")
      case _ => showUsage
    }
  }

  def code(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace(controllerPackage, namespaces)}
        |
        |${if (!namespaces.isEmpty) s"import _root_.${controllerPackage}._\n"}import skinny._
        |import skinny.validator._
        |
        |class ${toClassName(name)}Controller extends ApplicationController {
        |  protectFromForgery()
        |
        |  def index = render("${toResourcesBasePath(namespaces)}/${toVariable(name)}/index")
        |
        |}
        |""".stripMargin
  }

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

  def generate(namespaces: Seq[String], name: String): Unit = {
    val file = new File(
      s"${sourceDir}/${toDirectoryPath(controllerPackageDir, namespaces)}/${toClassName(name)}Controller.scala"
    )
    writeIfAbsent(file, code(namespaces, name))
  }

  override def appendToControllers(namespaces: Seq[String], name: String): Unit = {
    val controllerName = toControllerName(namespaces, name)
    val controllerClassName = toNamespace(s"_root_.${controllerPackage}", namespaces) + "." + toControllerClassName(
      name
    )
    val newMountCode =
      s"""def mount(ctx: ServletContext): Unit = {
        |    ${controllerName}.mount(ctx)""".stripMargin

    val path = {
      val parentPath = toDirectoryPath("", namespaces)
      if (parentPath.isEmpty) s"/${name}" else s"/${parentPath}/${name}"
    }
    val newControllerDefCode = {
      s"""  object ${controllerName} extends ${controllerClassName} with Routes {
      |    val indexUrl = get("${path}")(index).as("index")
      |  }
      |
      |}
      |""".stripMargin
    }

    val file = new File(s"${sourceDir}/${controllerPackageDir}/Controllers.scala")
    if (file.exists()) {
      val code = Source
        .fromFile(file)
        .mkString
        .replaceFirst("(def\\s+mount\\s*\\(ctx:\\s+ServletContext\\):\\s*Unit\\s*=\\s*\\{)", newMountCode)
        .replaceFirst("(}[\\s\\r\\n]+)$", newControllerDefCode)
      forceWrite(file, code)
    } else {
      val fullNewCode =
        s"""package ${controllerPackage}
          |
          |import _root_.${controllerPackage}._
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

  def controllerSpec(namespaces: Seq[String], name: String): String = {
    val namespace           = toNamespace(controllerPackage, namespaces)
    val controllerClassName = toClassName(name) + "Controller"
    val viewTemplatesPath   = s"${toResourcesBasePath(namespaces)}/${name}"

    s"""package ${namespace}
      |
      |import org.scalatest.funspec.AnyFunSpec
      |import org.scalatest.matchers.should.Matchers
      |import skinny._
      |import skinny.test._
      |import org.joda.time._
      |
      |// NOTICE before/after filters won't be executed by default
      |class ${controllerClassName}Spec extends AnyFunSpec with Matchers with DBSettings {
      |
      |  def createMockController = new ${controllerClassName} with MockController
      |
      |  describe("${controllerClassName}") {
      |
      |    it("shows index page") {
      |      val controller = createMockController
      |      controller.index
      |      controller.status should equal(200)
      |      controller.renderCall.map(_.path) should equal(Some("${viewTemplatesPath}/index"))
      |      controller.contentType should equal("text/html; charset=utf-8")
      |    }
      |
      |  }
      |
      |}
      |""".stripMargin
  }

  def generateControllerSpec(namespaces: Seq[String], name: String): Unit = {
    val specFile = new File(
      s"${testSourceDir}/${toDirectoryPath(controllerPackageDir, namespaces)}/${toClassName(name)}ControllerSpec.scala"
    )
    writeIfAbsent(specFile, controllerSpec(namespaces, name))
  }

  def integrationSpec(namespaces: Seq[String], name: String): String = {
    val path = {
      val parentPath = toDirectoryPath("", namespaces)
      if (parentPath.isEmpty) s"/${name}"
      else s"/${parentPath}/${name}"
    }

    s"""package ${toNamespace("integrationtest", namespaces)}
        |
        |import org.scalatest._
        |import skinny._
        |import skinny.test._
        |import org.joda.time._
        |import _root_.${controllerPackage}.Controllers
        |
        |class ${toClassName(name)}Controller_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport {
        |  addFilter(Controllers.${toControllerName(namespaces, name)}, "/*")
        |
        |  it should "show index page" in {
        |    get("${path}") {
        |      logBodyUnless(200)
        |      status should equal(200)
        |    }
        |  }
        |
        |}
        |""".stripMargin
  }

  def generateIntegrationSpec(namespaces: Seq[String], name: String): Unit = {
    val specFile = new File(
      s"${testSourceDir}/${toDirectoryPath("integrationtest", namespaces)}/${toClassName(name)}Controller_IntegrationTestSpec.scala"
    )
    writeIfAbsent(specFile, integrationSpec(namespaces, name))
  }

}
