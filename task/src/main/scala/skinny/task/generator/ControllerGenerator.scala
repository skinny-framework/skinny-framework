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

  def run(args: List[String]) {
    val completedArgs = if (args.size == 1) Seq("") ++ args
    else args

    completedArgs match {
      case namespace :: name :: _ =>
        val namespaces = namespace.split('.')
        showSkinnyGenerator()
        generateApplicationControllerIfAbsent()
        generate(namespaces, name)
        appendToControllers(namespaces, name)
        appendToScalatraBootstrap(name)
        generateSpec(namespaces, name)
        println("")
      case _ => showUsage
    }
  }

  def code(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace("controller", namespaces)}
        |
        |import skinny._
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

  def generate(namespaces: Seq[String], name: String) {
    val file = new File(s"src/main/scala/${toDirectoryPath("controller", namespaces)}/${toClassName(name)}Controller.scala")
    writeIfAbsent(file, code(namespaces, name))
  }

  def appendToControllers(namespaces: Seq[String], name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val newCode =
      s"""object Controllers {
        |  object ${toVariable(name)} extends ${toNamespace("controller", namespaces)}.${controllerClassName} with Routes {
        |    val indexUrl = get("/${toResourcesBasePath(namespaces)}/${toVariable(name)}/?")(index).as('index)
        |  }
        |""".stripMargin
    val file = new File("src/main/scala/controller/Controllers.scala")
    if (file.exists()) {
      val code = Source.fromFile(file).mkString.replaceFirst("object\\s+Controllers\\s*\\{", newCode)
      forceWrite(file, code)
    } else {
      forceWrite(file, newCode + "}\n")
    }
  }

  def appendToScalatraBootstrap(name: String) {
    val newCode =
      s"""override def initSkinnyApp(ctx: ServletContext) {
        |    Controllers.${toVariable(name)}.mount(ctx)
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

  def spec(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace("controller", namespaces)}
        |
        |import _root_.controller._
        |import _root_.model._
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |
        |class ${toClassName(name)}ControllerSpec extends ScalatraFlatSpec {
        |  addFilter(Controllers.${toVariable(name)}, "/*")
        |
        |}
        |""".stripMargin
  }

  def generateSpec(namespaces: Seq[String], name: String) {
    val specFile = new File(s"src/test/scala/${toDirectoryPath("controller", namespaces)}/${toClassName(name)}ControllerSpec.scala")
    writeIfAbsent(specFile, spec(namespaces, name))
  }

}
