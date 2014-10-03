package skinny.controller

import skinny._
import skinny.controller.assets._
import skinny.util.LoanPattern._
import scala.io.Source
import java.io.File
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

/**
 * Assets controller.
 */
class AssetsController extends SkinnyController {

  // see https://github.com/scalatra/scalatra/issues/349
  addMimeMapping("text/css", "css")
  addMimeMapping("application/octet-stream", "map")

  protected def sourceMapsEnabled: Boolean = SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()

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
   * Base path for assets files.
   */
  protected val basePath = "/WEB-INF/assets"

  /**
   * Registered JS Compilers
   */
  private[this] val jsCompilers = new collection.mutable.ListBuffer[AssetCompiler]

  /**
   * Registered CSS Compilers
   */
  private[this] val cssCompilers = new collection.mutable.ListBuffer[AssetCompiler]

  // registered compilers by default
  registerJsCompiler(CoffeeScriptAssetCompiler)
  registerJsCompiler(ReactJSXAssetCompiler)
  registerJsCompiler(ScalaJSAssetCompiler) // just provides Scala source code

  registerCssCompiler(LessAssetCompiler)
  registerCssCompiler(ScssAssetCompiler)
  registerCssCompiler(SassAssetCompiler)

  /**
   * Registers JS compiler to this controller.
   * @param compiler compiler
   */
  def registerJsCompiler(compiler: AssetCompiler) = jsCompilers.append(compiler)

  /**
   * Registers CSS compiler to this controller.
   * @param compiler compiler
   */
  def registerCssCompiler(compiler: AssetCompiler) = cssCompilers.append(compiler)

  def path(extension: String): Option[String] = multiParams("splat").headOption.flatMap { fullPath =>
    fullPath.split("\\.") match {
      case Array(path, e) if e == extension => Some(path)
      case _ => None
    }
  }
  def sourceMapsPath(): Option[String] = path("map")

  /**
   * Returns js or coffee assets.
   */
  def js(): Any = if (path("js") == Some("skinny-framework")) {
    path("js").flatMap { p =>
      contentType = "application/javascript"
      jsFromClassPath(p)
    }.getOrElse(
      throw new IllegalStateException("skinny-framework.js should be found. This is a framework bug.")
    )
  } else {
    if (isEnabled) {
      path("js").map { path =>
        jsFromClassPath(path)
          .orElse(compiledJsFromClassPath(path))
          .orElse(jsFromFile(path))
          .orElse(compiledJsFromFile(path))
          .map { js =>
            contentType = "application/javascript"
            js
          }.getOrElse(pass())
      }.orElse(jsSourceMapsFile()).getOrElse(pass())
    } else pass()
  }

  private def jsSourceMapsFile(): Option[Any] = {
    if (sourceMapsEnabled) {
      sourceMapsPath.flatMap { path =>
        contentType = "application/octet-stream"
        sourceMapsFromFile(path, jsCompilers)
      }.orElse {
        jsCompilers.find(c => path(c.extension).isDefined).flatMap { compiler =>
          path(compiler.extension).map { path =>
            contentType = "application/octet-stream"
            val file = new File(servletContext.getRealPath(s"${basePath}/${compiler.extension}/${path}.${compiler.extension}"))
            using(Source.fromFile(file))(map => map.mkString)
          }
        }
      }
    } else None
  }

  private def jsFromClassPath(path: String): Option[String] = {
    ClassPathResourceLoader.getClassPathResource(s"${basePath}/js/${path}.js").map { resource =>
      using(resource.stream) { stream =>
        setLastModified(resource.lastModified)
        if (isModified(resource.lastModified)) using(Source.fromInputStream(resource.stream))(_.mkString)
        else halt(304)
      }
    }
  }
  private def jsFromFile(path: String): Option[String] = {
    val jsFile = new File(servletContext.getRealPath(s"${basePath}/js/${path}.js"))
    if (jsFile.exists()) {
      setLastModified(jsFile.lastModified)
      if (isModified(jsFile.lastModified)) Some(using(Source.fromFile(jsFile))(js => js.mkString))
      else halt(304)
    } else None
  }
  private def compiledJsFromClassPath(path: String): Option[String] = compiledCodeFromClassPath(path, jsCompilers)
  private def compiledJsFromFile(path: String): Option[String] = compiledCodeFromFile(path, jsCompilers)

  private def sourceMapsFromFile(path: String, compilers: Seq[AssetCompiler]): Option[String] = {
    compilers.find { compiler =>
      new File(servletContext.getRealPath(s"${basePath}/${compiler.extension}/${path}.map")).exists()
    }.map { compiler =>
      val mapFile = new File(servletContext.getRealPath(s"${basePath}/${compiler.extension}/${path}.map"))
      setLastModified(mapFile.lastModified)
      if (isModified(mapFile.lastModified)) using(Source.fromFile(mapFile))(map => map.mkString)
      else halt(304)
    }
  }

  /**
   * Returns css or less assets.
   */
  def css(): Any = if (isEnabled) {
    multiParams("splat").headOption.flatMap { fullPath =>
      fullPath.split("\\.") match {
        case Array(path, "css") => Some(path)
        case _ => None
      }
    }.map { path =>
      cssFromClassPath(path)
        .orElse(compiledCssFromClassPath(path))
        .orElse(cssFromFile(path))
        .orElse(compiledCssFromFile(path))
        .map { css =>
          contentType = "text/css"
          css
        }.getOrElse(pass())
    }.orElse(cssSourceMapsFile()).getOrElse(pass())
  } else pass()

  private def cssSourceMapsFile(): Option[Any] = {
    if (sourceMapsEnabled) {
      sourceMapsPath.flatMap { path =>
        contentType = "application/octet-stream"
        sourceMapsFromFile(path, cssCompilers)
      }.orElse {
        cssCompilers.find(c => path(c.extension).isDefined).flatMap { compiler =>
          path(compiler.extension).map { path =>
            contentType = "application/octet-stream"
            val file = new File(servletContext.getRealPath(s"${basePath}/${compiler.extension}/${path}.${compiler.extension}"))
            using(Source.fromFile(file))(map => map.mkString)
          }
        }
      }
    } else None
  }

  def cssFromClassPath(path: String): Option[String] = {
    ClassPathResourceLoader.getClassPathResource(s"${basePath}/css/${path}.css").map { resource =>
      using(resource.stream) { stream =>
        setLastModified(resource.lastModified)
        if (isModified(resource.lastModified)) {
          using(Source.fromInputStream(resource.stream))(_.mkString)
        } else halt(304)
      }
    }
  }
  private def cssFromFile(path: String): Option[String] = {
    val cssFile = new File(servletContext.getRealPath(s"${basePath}/css/${path}.css"))
    if (cssFile.exists()) {
      setLastModified(cssFile.lastModified)
      if (isModified(cssFile.lastModified)) Some(using(Source.fromFile(cssFile))(css => css.mkString))
      else halt(304)
    } else None
  }
  private def compiledCssFromClassPath(path: String): Option[String] = compiledCodeFromClassPath(path, cssCompilers)
  private def compiledCssFromFile(path: String): Option[String] = compiledCodeFromFile(path, cssCompilers)

  protected val PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz"
  protected val PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz"
  protected val PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy"

  protected val modifiedHeaderFormats = Seq(PATTERN_RFC1123, PATTERN_RFC1036, PATTERN_ASCTIME).map { pattern =>
    DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.UTC).withLocale(java.util.Locale.ENGLISH)
  }

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

  private def compiledCodeFromClassPath(path: String, compilers: Seq[AssetCompiler]): Option[String] = {
    // try to load from class path resources
    compilers.flatMap(c => c.findClassPathResource(basePath, path).map(r => (c, r))).headOption.map {
      case (compiler, resource) => using(resource.stream) { stream =>
        setLastModified(resource.lastModified)
        if (isModified(resource.lastModified)) {
          compiler.compile(path, using(Source.fromInputStream(resource.stream))(_.mkString))
        } else halt(304)
      }
    }
  }

  private def compiledCodeFromFile(path: String, compilers: Seq[AssetCompiler]): Option[String] = {
    // load content from real files
    compilers.flatMap { c =>
      val file = c.findRealFile(servletContext, basePath, path)
      if (file.exists) Some((c, file)) else None
    }.headOption.map {
      case (compiler, file) =>
        setLastModified(file.lastModified)
        if (isModified(file.lastModified)) {
          using(Source.fromFile(file))(code => compiler.compile(file.getPath, code.mkString))
        } else halt(304)
    }
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

