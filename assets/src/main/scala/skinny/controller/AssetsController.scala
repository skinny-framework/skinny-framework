package skinny.controller

import skinny._, assets._, LoanPattern._
import scala.io.Source
import java.io.File
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

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
      val jsResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/js/${path}.js")
      val coffeeResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/coffee/${path}.coffee")
      if (jsResource.isDefined) {
        jsResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isNotModified(resource.lastModified)) halt(304)
            else using(Source.fromInputStream(resource.stream))(_.mkString)
          }
        } getOrElse (halt(404))
      } else if (coffeeResource.isDefined) {
        coffeeResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isNotModified(resource.lastModified)) halt(304)
            else coffeeScriptCompiler.compile(using(Source.fromInputStream(resource.stream))(_.mkString))
          }
        }.getOrElse(halt(404))

      } else {
        // load content from real files
        val jsFile = new File(servletContext.getRealPath(s"${basePath}/js/${path}.js"))
        val coffeeFile = new File(servletContext.getRealPath(s"${basePath}/coffee/${path}.coffee"))
        if (jsFile.exists()) {
          setLastModified(jsFile.lastModified)
          if (isNotModified(jsFile.lastModified)) halt(304)
          else using(Source.fromFile(jsFile))(js => js.mkString)
        } else if (coffeeFile.exists()) {
          setLastModified(coffeeFile.lastModified)
          if (isNotModified(coffeeFile.lastModified)) halt(304)
          else using(Source.fromFile(coffeeFile))(coffee => coffeeScriptCompiler.compile(coffee.mkString))
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
      val cssResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/css/${path}.css")
      val lessResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/less/${path}.less")
      if (cssResource.isDefined) {
        cssResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isNotModified(resource.lastModified)) halt(304)
            else using(Source.fromInputStream(resource.stream))(_.mkString)
          }
        } getOrElse (halt(404))
      } else if (lessResource.isDefined) {
        lessResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isNotModified(resource.lastModified)) halt(304)
            else lessCompiler.compile(using(Source.fromInputStream(resource.stream))(_.mkString))
          }
        }.getOrElse(halt(404))

      } else {
        // load content from real files
        val cssFile = new File(servletContext.getRealPath(s"${basePath}/css/${path}.css"))
        val lessFile = new File(servletContext.getRealPath(s"${basePath}/less/${path}.less"))
        if (cssFile.exists()) {
          setLastModified(cssFile.lastModified)
          if (isNotModified(cssFile.lastModified)) halt(304)
          else using(Source.fromFile(cssFile))(js => js.mkString)
        } else if (lessFile.exists()) {
          setLastModified(lessFile.lastModified)
          if (isNotModified(lessFile.lastModified)) halt(304)
          else using(Source.fromFile(lessFile))(less => lessCompiler.compile(less.mkString))
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

  protected val PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz"
  protected val PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz"
  protected val PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy"

  protected val modifiedHeaderFormats = Seq(
    DateTimeFormat.forPattern(PATTERN_RFC1123).withZone(DateTimeZone.UTC),
    DateTimeFormat.forPattern(PATTERN_RFC1036).withZone(DateTimeZone.UTC),
    DateTimeFormat.forPattern(PATTERN_ASCTIME).withZone(DateTimeZone.UTC)
  )

  protected def setLastModified(lastModified: Long): Unit = {
    val format =modifiedHeaderFormats.head
    response.setHeader("Last-Modified", format.print(lastModified).replaceFirst("UTC$", "GMT"))
  }

  protected def isNotModified(lastModified: Long): Boolean = {
    request.header("If-Modified-Since").map(_.replaceFirst("^\"", "").replaceFirst("\"$", "")).map { ifModifiedSince =>
      modifiedHeaderFormats.flatMap { formatter =>
        try Option(formatter.parseDateTime(ifModifiedSince))
        catch { case e: Exception => None }
      }.headOption.map(_.getMillis <= lastModified) getOrElse false
    } getOrElse false
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

