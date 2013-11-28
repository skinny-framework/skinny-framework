package skinny.controller

import skinny._, assets._
import skinny.util.LoanPattern._
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
   * LESS compiler.
   */
  protected val lessCompiler = LessCompiler

  /**
   * Sass compiler.
   */
  protected val sassCompiler = SassCompiler

  /**
   * Base path for assets files.
   */
  protected val basePath = "/WEB-INF/assets"

  /**
   * Registered JS Compilers
   */
  private[this] val jsCompilers = new collection.mutable.ListBuffer[AssetCompiler]

  /**
   * Registers JS compiler to this controller.
   * @param compiler compiler
   */
  def registerJsCompiler(compiler: AssetCompiler) = jsCompilers.append(compiler)

  // registered compilers by default
  registerJsCompiler(CoffeeScriptAssetCompiler)

  /**
   * Returns js or coffee assets.
   */
  def js(): Any = if (isEnabled) {
    multiParams("splat").headOption.flatMap {
      _.split("\\.") match {
        case Array(path, "js") => Some(path)
        case _ => None
      }
    }.map { path =>
      jsFromClassPath(path) orElse compiledJsFromClassPath(path) orElse compiledJsFromFile(path) getOrElse pass()
    }.getOrElse { pass() }
  } else {
    pass()
  }

  private def jsFromClassPath(path: String): Option[String] = {
    ClassPathResourceLoader.getClassPathResource(s"${basePath}/js/${path}.js").map { resource =>
      using(resource.stream) { stream =>
        setLastModified(resource.lastModified)
        if (isModified(resource.lastModified))
          using(Source.fromInputStream(resource.stream))(_.mkString)
        else halt(304)
      }
    }
  }

  private def compiledJsFromClassPath(path: String): Option[String] = {
    // try to load from class path resources
    jsCompilers.flatMap(c => c.findClassPathResource(basePath, path).map(r => (c, r))).headOption.map {
      case (compiler, resource) =>
        using(resource.stream) { stream =>
          setLastModified(resource.lastModified)
          if (isModified(resource.lastModified)) {
            compiler.compile(using(Source.fromInputStream(resource.stream))(_.mkString))
          } else halt(304)
        }
    }
  }

  private def compiledJsFromFile(path: String): Option[String] = {
    // load content from real files
    val jsFile = new File(servletContext.getRealPath(s"${basePath}/js/${path}.js"))
    if (jsFile.exists()) {
      setLastModified(jsFile.lastModified)
      if (isModified(jsFile.lastModified)) Some(using(Source.fromFile(jsFile))(js => js.mkString))
      else halt(304)
    } else if (jsCompilers.exists(_.findRealFile(servletContext, basePath, path).exists)) {
      jsCompilers.find(_.findRealFile(servletContext, basePath, path).exists).map { compiler =>
        val file = compiler.findRealFile(servletContext, basePath, path)
        setLastModified(file.lastModified)
        if (isModified(file.lastModified)) {
          using(Source.fromFile(file))(code => compiler.compile(code.mkString))
        } else halt(304)
      }
    } else {
      pass()
    }
  }

  // TODO refactoring

  /**
   * Returns css or less assets.
   */
  def css(): Any = if (isEnabled) {
    multiParams("splat").headOption.flatMap {
      _.split("\\.") match {
        case Array(path, "css") => Some(path)
        case _ => None
      }
    }.map { path =>

      // try to load from class path resources
      val cssResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/css/${path}.css")
      val lessResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/less/${path}.less")
      val scssResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/scss/${path}.scss").orElse(
        ClassPathResourceLoader.getClassPathResource(s"${basePath}/sass/${path}.scss"))
      val sassResource = ClassPathResourceLoader.getClassPathResource(s"${basePath}/sass/${path}.sass")

      if (cssResource.isDefined) {
        // CSS
        cssResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isModified(resource.lastModified)) {
              using(Source.fromInputStream(resource.stream))(_.mkString)
            } else halt(304)
          }
        } getOrElse (halt(404))

      } else if (lessResource.isDefined) {
        // LESS
        lessResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isModified(resource.lastModified)) {
              lessCompiler.compile(using(Source.fromInputStream(resource.stream))(_.mkString))
            } else halt(304)
          }
        }.getOrElse(halt(404))

      } else if (scssResource.isDefined) {
        // Sass - scss
        scssResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isModified(resource.lastModified)) {
              sassCompiler.compile(using(Source.fromInputStream(resource.stream))(_.mkString))
            } else halt(304)
          }
        }.getOrElse(halt(404))

      } else if (sassResource.isDefined) {
        // Sass - sass
        sassResource.map { resource =>
          using(resource.stream) { stream =>
            setLastModified(resource.lastModified)
            if (isModified(resource.lastModified)) {
              sassCompiler.compileIndented(using(Source.fromInputStream(resource.stream))(_.mkString))
            } else halt(304)
          }
        }.getOrElse(halt(404))

      } else {
        // load content from real files for development

        val cssFile = new File(servletContext.getRealPath(s"${basePath}/css/${path}.css"))
        val lessFile = new File(servletContext.getRealPath(s"${basePath}/less/${path}.less"))
        val scssFile = {
          val inScssDir = new File(servletContext.getRealPath(s"${basePath}/scss/${path}.scss"))
          if (inScssDir.exists) inScssDir
          else new File(servletContext.getRealPath(s"${basePath}/sass/${path}.scss"))
        }
        val sassFile = new File(servletContext.getRealPath(s"${basePath}/sass/${path}.sass"))
        if (cssFile.exists()) {
          setLastModified(cssFile.lastModified)
          if (isModified(cssFile.lastModified)) using(Source.fromFile(cssFile))(js => js.mkString)
          else halt(304)
        } else if (lessFile.exists()) {
          setLastModified(lessFile.lastModified)
          if (isModified(lessFile.lastModified)) {
            using(Source.fromFile(lessFile))(less => lessCompiler.compile(less.mkString))
          } else halt(304)
        } else if (scssFile.exists()) {
          setLastModified(scssFile.lastModified)
          if (isModified(scssFile.lastModified)) {
            using(Source.fromFile(scssFile))(scss => sassCompiler.compile(scss.mkString))
          } else halt(304)
        } else if (sassFile.exists()) {
          setLastModified(sassFile.lastModified)
          if (isModified(sassFile.lastModified)) {
            using(Source.fromFile(sassFile))(sass => sassCompiler.compileIndented(sass.mkString))
          } else halt(304)
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
    val format = modifiedHeaderFormats.head
    response.setHeader("Last-Modified", format.print(lastModified).replaceFirst("UTC$", "GMT"))
  }

  protected def isModified(resourceLastModified: Long): Boolean = {
    request.header("If-Modified-Since").map(_.replaceFirst("^\"", "").replaceFirst("\"$", "")).map { ifModifiedSince =>
      modifiedHeaderFormats.flatMap { formatter =>
        try Option(formatter.parseDateTime(ifModifiedSince))
        catch { case e: Exception => None }
      }.headOption.map(_.getMillis < resourceLastModified) getOrElse true
    } getOrElse true
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

