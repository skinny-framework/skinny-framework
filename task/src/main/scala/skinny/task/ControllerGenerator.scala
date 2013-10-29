package skinny.task

import java.io.File

/**
 * Controller generator.
 */
object ControllerGenerator extends ControllerGenerator

trait ControllerGenerator extends CodeGenerator {

  private def showUsage = {
    println("Usage: sbt \"task/run g controller members name:String birthday:Option[LocalDate]\"")
  }

  def run(args: List[String]) {
    args.toList match {
      case name :: _ => generate(name)
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
        |  def index = render("/${name}/index")
        |
        |  // src/main/scala/controller/Controllers.scala
        |  // object Controllers {
        |  //   val ${toVariable(name)} = new ${controllerClassName} with Routes {
        |  //     get("/${toVariable(name)}/?")(index).as('index)
        |  //   }
        |  // }
        |
        |  // src/main/scala/ScalatraBootstrap.scala
        |  // class ScalatraBootstrap extends SkinnyLifeCycle {
        |  //   override def initSkinnyApp(ctx: ServletContext) {
        |  //     Controllers.${toVariable(name)}.mount(ctx)
        |  //   }
        |  // }
        |
        |}
        |""".stripMargin
    writeIfAbsent(productionFile, productionCode)
  }

  def generateSpec(name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val specFile = new File(s"src/test/scala/controller/${controllerClassName}Spec.scala")
    val specCode =
      s"""package controller
        |
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |import model._
        |
        |class ${controllerClassName}Spec extends ScalatraFlatSpec {
        |  addFilter(${controllerClassName}, "/*")
        |
        |}
        |""".stripMargin
    writeIfAbsent(specFile, specCode)
  }

}
