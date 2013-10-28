package skinny.controller

import skinny._, assets._, LoanPattern._
import scala.io.Source
import java.io.File

/**
 * Assets controller.
 */
class AssetsController extends SkinnyController {

  /**
   * Returns assets root path.
   */
  protected def assetsRootPath = "/assets"

  /**
   * Returns assets/js root path.
   */
  protected def jsRootPath = s"${assetsRootPath}/js"

  /**
   * Returns assets/css root path.
   */
  protected def cssRootPath = s"${assetsRootPath}/css"

  /**
   * Predicates this controller in staging env.
   */
  protected def isDisabledInStaging: Boolean = true

  /**
   * Predicates this controller in production env.
   */
  protected def isDisabledInProduction: Boolean = true

  /**
   * Predicates this controller is enabled in the current env.
   */
  protected def isEnabled: Boolean = {
    if (SkinnyEnv.isProduction()) !isDisabledInProduction
    if (SkinnyEnv.isStaging()) !isDisabledInStaging
    else true
  }

  /**
   * CoffeeScript compiler.
   */
  protected val coffeeScriptCompiler = CoffeeScriptCompiler()

  /**
   * LESS compiler.
   */
  protected val lessCompiler = LessCompiler

  /**
   * Base path for assets files.
   */
  protected val basePath = "/WEB-INF/assets"

  /**
   * Returns js or coffee assets.
   */
  def js() = if (isEnabled) {
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

  /**
   * Returns css or less assets.
   */
  def css() = if (isEnabled) {
    multiParams("splat").headOption.flatMap {
      _.split("\\.") match {
        case Array(path, "css") => Some(path)
        case _ => None
      }
    }.map { path =>

      // try to load from class path resources
      val cssResource = ClassPathResourceLoader.getResourceAsStream(s"${basePath}/css/${path}.css")
      val lessResource = ClassPathResourceLoader.getResourceAsStream(s"${basePath}/less/${path}.less")
      if (cssResource.isDefined) {
        cssResource.map { resource =>
          using(Source.fromInputStream(resource))(_.mkString)
        } getOrElse (halt(404))
      } else if (lessResource.isDefined) {
        lessResource.map { resource =>
          lessCompiler.compile(using(Source.fromInputStream(resource))(_.mkString))
        }.getOrElse(halt(404))

      } else {
        // load content from real files
        val cssFile = new File(servletContext.getRealPath(s"${basePath}/css/${path}.css"))
        val lessFile = new File(servletContext.getRealPath(s"${basePath}/less/${path}.less"))
        if (cssFile.exists()) {
          using(Source.fromFile(cssFile))(js => js.mkString)
        } else if (lessFile.exists()) {
          using(Source.fromFile(lessFile))(coffee => lessCompiler.compile(coffee.mkString))
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

/**
 * AssetsController with default configurations.
 */
object AssetsController extends AssetsController with Routes {

  // Unfortunately, *.* seems not to work.
  get(s"${jsRootPath}/*")(js).as('js)
  get(s"${cssRootPath}/*")(css).as('css)

}

