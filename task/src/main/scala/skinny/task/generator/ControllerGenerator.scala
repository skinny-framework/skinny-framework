package skinny.task.generator

import java.io.File
import scala.io.Source

/**
 * Controller generator.
 */
object ControllerGenerator extends ControllerGenerator

trait ControllerGenerator extends CodeGenerator {

  private[this] def showUsage = {
    println("Usage: sbt \"task/run generate-controller members name:String birthday:Option[LocalDate]\"")
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

  def code(name: String): String = {
    s"""package controller
        |
        |import skinny._
        |import skinny.validator._
        |
        |class ${toClassName(name)}Controller extends SkinnyController {
        |  protectFromForgery()
        |
        |  def index = render("/${toVariable(name)}/index")
        |
        |}
        |""".stripMargin
  }

  def generate(name: String) {
    val file = new File(s"src/main/scala/controller/${toClassName(name)}Controller.scala")
    writeIfAbsent(file, code(name))
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

  def spec(name: String): String = {
    s"""package controller
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

  def generateSpec(name: String) {
    val specFile = new File(s"src/test/scala/controller/${toClassName(name)}ControllerSpec.scala")
    writeIfAbsent(specFile, spec(name))
  }

}
