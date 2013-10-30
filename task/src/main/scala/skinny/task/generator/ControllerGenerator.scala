package skinny.task.generator

import java.io.File
import scala.io.Source

/**
 * Controller generator.
 */
object ControllerGenerator extends ControllerGenerator

trait ControllerGenerator extends CodeGenerator {

  private def showUsage = {
    println("Usage: sbt \"task/run g generate-controller members name:String birthday:Option[LocalDate]\"")
  }

  def run(args: List[String]) {
    args.toList match {
      case name :: _ =>
        generate(name)
        appendToControllers(name)
        appendToScalatraBootstrap(name)
        generateSpec(name)
      case _ => showUsage
    }
  }

  def generate(name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val productionFile = new File(s"src/main/scala/controller/${controllerClassName}.scala")
    val productionCode =
      s"""package controller
        |
        |import skinny._
        |import skinny.validator._
        |
        |class ${controllerClassName} extends SkinnyController {
        |  protectFromForgery()
        |
        |  def index = render("/${toVariable(name)}/index")
        |
        |}
        |""".stripMargin
    writeIfAbsent(productionFile, productionCode)
  }

  def appendToControllers(name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val newCode =
      s"""object Controllers {
        |  val ${toVariable(name)} = new ${controllerClassName} with Routes {
        |    get("/${toVariable(name)}/?")(index).as('index)
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

  def generateSpec(name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val specFile = new File(s"src/test/scala/controller/${controllerClassName}Spec.scala")
    val specCode =
      s"""package controller
        |
        |import _root_.controller._
        |import _root_.model._
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |
        |class ${controllerClassName}Spec extends ScalatraFlatSpec {
        |  addFilter(Controllers.${toVariable(name)}, "/*")
        |
        |}
        |""".stripMargin
    writeIfAbsent(specFile, specCode)
  }

}
