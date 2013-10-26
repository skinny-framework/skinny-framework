package skinny.controller

import skinny._, assets._, LoanPattern._
import scala.io.Source
import java.io.File

/**
 * Assets controller.
 */
class AssetsController extends SkinnyController {

  val isEnabledInProduction: Boolean = false

  def isEnabled: Boolean = if (isEnabledInProduction) true else !SkinnyEnv.isProduction()

  val coffeeScriptCompiler = CoffeeScriptCompiler()

  private[this] val basePath = "/WEB-INF/assets/"

  def jsOrCoffee() = if (isEnabled) {
    multiParams("splat").headOption.flatMap {
      _.split("\\.") match {
        case Array(path, "js") => Some(path)
        case _ => None
      }
    }.map { path =>

      // try to load from class path resources
      val jsResource = ClassPathResourceLoader.getResourceAsStream(s"${basePath}/js/${path}.js")
      val coffeeResource = ClassPathResourceLoader.getResourceAsStream(s"${basePath}/coffee/${path}.coffee")
      if (jsResource.isDefined) {
        jsResource.map { resource =>
          using(Source.fromInputStream(resource))(_.mkString)
        } getOrElse (halt(404))
      } else if (coffeeResource.isDefined) {
        coffeeResource.map { resource =>
          coffeeScriptCompiler.compile(using(Source.fromInputStream(resource))(_.mkString))
        }.getOrElse(halt(404))

      } else {
        // load content from real files
        val jsFile = new File(servletContext.getRealPath(s"${basePath}/js/${path}.js"))
        val coffeeFile = new File(servletContext.getRealPath(s"${basePath}/coffee/${path}.coffee"))
        if (jsFile.exists()) {
          using(Source.fromFile(jsFile))(js => js.mkString)
        } else if (coffeeFile.exists()) {
          using(Source.fromFile(coffeeFile))(coffee => coffeeScriptCompiler.compile(coffee.mkString))
        } else {
          pass()
        }

      }
    } getOrElse {
      pass()
    }
  } else {
    pass()
  }

}

object AssetsController extends AssetsController with Routes {

  override val isEnabledInProduction = false

  // Unfortunately, *.* seems not to work.
  get("/assets/js/*")(jsOrCoffee).as('jsOrCoffee)
}
