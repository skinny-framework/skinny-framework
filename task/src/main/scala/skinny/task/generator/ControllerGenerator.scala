package skinny.task.generator

import java.io.File

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
        generateSpec(namespaces, name)
        println("")
      case _ => showUsage
    }
  }

  def code(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace("controller", namespaces)}
        |
        |${if (!namespaces.isEmpty) "import _root_.controller._\n"}import skinny._
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

  def spec(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace("controller", namespaces)}
        |
        |import _root_.controller._
        |import _root_.model._
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |
        |class ${toClassName(name)}ControllerSpec extends ScalatraFlatSpec {
        |  addFilter(Controllers.${toControllerName(namespaces, name)}, "/*")
        |
        |}
        |""".stripMargin
  }

  def generateSpec(namespaces: Seq[String], name: String) {
    val specFile = new File(s"src/test/scala/${toDirectoryPath("controller", namespaces)}/${toClassName(name)}ControllerSpec.scala")
    writeIfAbsent(specFile, spec(namespaces, name))
  }

}
