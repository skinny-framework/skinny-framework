package skinny.task

import java.io.File
import org.apache.commons.io.FileUtils

/**
 * Controller generator.
 */
object ControllerGenerator extends ControllerGenerator

trait ControllerGenerator {

  def run(args: List[String]) {
    args.toList match {
      case name :: _ =>
        generate(name)
      case _ =>
        println("Usage: sbt \"task/run g model member name:String birthday:Option[LocalDate]\"")
    }
  }

  def generate(name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val productionFile = new File(s"src/main/scala/controller/${controllerClassName}.scala")
    FileUtils.forceMkdir(productionFile.getParentFile)
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
        |  // val ${name} = new ${controllerClassName} with Routes {
        |  //   get("/${name}/?")(index).as('index)
        |  // }
        |
        |  // src/main/scala/ScalatraBootstrap.scala
        |  // class ScalatraBootstrap extends SkinnyLifeCycle {
        |  //   override def initSkinnyApp(ctx: ServletContext) {
        |  //     Controllers.${name}.mount(ctx)
        |  //   }
        |  // }
        |
        |}
        |""".stripMargin
    FileUtils.write(productionFile, productionCode)
    println(s"${productionFile.getAbsolutePath} is created.")
  }

  def generateSpec(name: String) {
    val controllerClassName = s"${name.head.toUpper + name.tail}Controller"
    val specFile = new File(s"src/test/scala/controller/${controllerClassName}Spec.scala")
    FileUtils.forceMkdir(specFile.getParentFile)
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
    FileUtils.write(specFile, specCode)
    println(s"${specFile.getAbsolutePath} is created.")
  }

}
