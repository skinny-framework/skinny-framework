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

  def sourceMapsEnabled: Boolean = SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()

  /**
   * Returns assets root path.
   */
  def assetsRootPath = "/assets"

  /**
   * Returns assets/js root path.
   */
  def jsRootPath = s"${assetsRootPath}/js"

  /**
   * Returns assets/css root path.
   */
  def cssRootPath = s"${assetsRootPath}/css"

  /**
   * Predicates this controller in staging env.
   */
  def isDisabledInStaging: Boolean = true

  /**
   * Predicates this controller in production env.
   */
  def isDisabledInProduction: Boolean = true

  /**
   * Predicates this controller is enabled in the current env.
   */
  def isEnabled: Boolean = {
    if (SkinnyEnv.isProduction()) !isDisabledInProduction
    else if (SkinnyEnv.isStaging()) !isDisabledInStaging
    else true
  }

  /**
   * Base path for assets files.
   */
  val basePath = "/WEB-INF/assets"
  val publicBasePath = "/assets"

  /**
   * Registered JS Compilers
   */
  private[this] val jsCompilers = new scala.collection.mutable.ListBuffer[AssetCompiler]

  /**
   * Registered CSS Compilers
   */
  private[this] val cssCompilers = new scala.collection.mutable.ListBuffer[AssetCompiler]

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

  private[this] val skinnyJsNotFoundMessage = "skinny-framework.js should be found. This is a framework bug."

  /**
   * Returns js or coffee assets.
   */
  def js(): Any = {
    path("js") match {
      case Some("skinny-framework") =>
        jsFromClassPath("skinny-framework") match {
          case Some(js) =>
            contentType = "application/javascript"
            js
          case _ => throw new IllegalStateException(skinnyJsNotFoundMessage)
        }
      case _ if isEnabled =>
        path("js") match {
          case Some(path) =>
            val jsFound: Option[String] = {
              jsFromClassPath(path)
                .orElse(compiledJsFromClassPath(path))
                .orElse(jsFromFile(path))
                .orElse(compiledJsFromFile(path))
                .map { js =>
                  contentType = "application/javascript"
                  js
                }
            }
            jsFound match {
              case Some(js) => js
              case _ => pass()
            }
          case _ => jsSourceMapsFile().getOrElse(pass())
        }
      case _ => pass()
    }
  }

  private def jsSourceMapsFile(): Option[Any] = {
    if (sourceMapsEnabled) {
      sourceMapsPath match {
        case Some(path) =>
          contentType = "application/octet-stream"
          sourceMapsFromFile(path, jsCompilers)
        case _ =>
          jsCompilers.find(c => path(c.extension).isDefined).flatMap { compiler =>
            path(compiler.extension).map { path =>
              contentType = "application/octet-stream"
              val foundFile: Option[File] = {
                Seq(s"${basePath}/${compiler.extension}/${path}.${compiler.extension}",
                  s"${publicBasePath}/${compiler.extension}/${path}.${compiler.extension}" // basically won't be found here
                ).map(p => new File(servletContext.getRealPath(p))).find(_.exists())
              }
              foundFile match {
                case Some(file) => using(Source.fromFile(file))(_.mkString)
                case _ => ""
              }
            }
          }
      }
    } else None
  }

  private def jsFromClassPath(path: String): Option[String] = {
    def findResource(path: String): Option[ClassPathResource] = {
      ClassPathResourceLoader.getClassPathResource(s"${publicBasePath}/js/${path}.js")
        .orElse(ClassPathResourceLoader.getClassPathResource(s"${basePath}/js/${path}.js"))
    }
    findResource(path).map { resource =>
      using(resource.stream) { stream =>
        setLastModified(resource.lastModified)
        if (isModified(resource.lastModified)) using(Source.fromInputStream(resource.stream))(_.mkString)
        else halt(304)
      }
    }
  }
  private def jsFromFile(path: String): Option[String] = {
    val foundFile: Option[File] = {
      Seq(s"${publicBasePath}/js/${path}.js", s"${basePath}/js/${path}.js")
        .map(p => new File(servletContext.getRealPath(p))).find(_.exists())
    }
    foundFile match {
      case Some(foundJsFile) =>
        setLastModified(foundJsFile.lastModified)
        if (isModified(foundJsFile.lastModified)) Some(using(Source.fromFile(foundJsFile))(js => js.mkString))
        else halt(304)
      case _ => None
    }
  }
  private def compiledJsFromClassPath(path: String): Option[String] = compiledCodeFromClassPath(path, jsCompilers)
  private def compiledJsFromFile(path: String): Option[String] = compiledCodeFromFile(path, jsCompilers)

  private def sourceMapsFromFile(path: String, compilers: Seq[AssetCompiler]): Option[String] = {
    def findFile(path: String, extension: String): Option[File] = {
      Seq(s"${publicBasePath}/${extension}/${path}.map", s"${basePath}/${extension}/${path}.map")
        .map(path => new File(servletContext.getRealPath(path))).find(_.exists())
    }
    compilers
      .find { compiler => findFile(path, compiler.extension).isDefined }
      .map { compiler =>
        val mapFile: File = findFile(path, compiler.extension).getOrElse {
          throw new IllegalStateException("this source map file must exist")
        }
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
        cssCompilers.find(c => path(c.extension).isDefined) match {
          case Some(compiler) =>
            path(compiler.extension) match {
              case Some(path) =>
                contentType = "application/octet-stream"
                val foundFile: Option[File] = Seq(
                  s"${publicBasePath}/${compiler.extension}/${path}.${compiler.extension}",
                  s"${basePath}/${compiler.extension}/${path}.${compiler.extension}"
                ).map(path => new File(servletContext.getRealPath(path))).find(_.exists())
                foundFile match {
                  case Some(file) => Some(using(Source.fromFile(file))(map => map.mkString))
                  case _ => None
                }
              case _ => None
            }
          case _ => None
        }
      }
    } else None
  }

  def cssFromClassPath(path: String): Option[String] = {
    def findResource(path: String): Option[ClassPathResource] = {
      ClassPathResourceLoader.getClassPathResource(s"${publicBasePath}/css/${path}.css")
        .orElse(ClassPathResourceLoader.getClassPathResource(s"${basePath}/css/${path}.css"))
    }
    findResource(path).map { resource =>
      using(resource.stream) { stream =>
        setLastModified(resource.lastModified)
        if (isModified(resource.lastModified)) {
          using(Source.fromInputStream(resource.stream))(_.mkString)
        } else halt(304)
      }
    }
  }
  private def cssFromFile(path: String): Option[String] = {
    val foundFile: Option[File] = {
      Seq(s"${publicBasePath}/css/${path}.css", s"${basePath}/css/${path}.css")
        .map(path => new File(servletContext.getRealPath(path))).find(_.exists())
    }
    foundFile match {
      case Some(cssFile) =>
        setLastModified(cssFile.lastModified)
        if (isModified(cssFile.lastModified)) Some(using(Source.fromFile(cssFile))(css => css.mkString))
        else halt(304)
      case _ =>
        None
    }
  }
  private def compiledCssFromClassPath(path: String): Option[String] = compiledCodeFromClassPath(path, cssCompilers)
  private def compiledCssFromFile(path: String): Option[String] = compiledCodeFromFile(path, cssCompilers)

  val PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz"
  val PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz"
  val PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy"

  val modifiedHeaderFormats = Seq(PATTERN_RFC1123, PATTERN_RFC1036, PATTERN_ASCTIME).map { pattern =>
    DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.UTC).withLocale(java.util.Locale.ENGLISH)
  }

  def setLastModified(lastModified: Long): Unit = {
    val format = modifiedHeaderFormats.head
    response.setHeader("Last-Modified", format.print(lastModified).replaceFirst("UTC$", "GMT"))
  }

  def isModified(resourceLastModified: Long): Boolean = {
    request.header("If-Modified-Since").map(_.replaceFirst("^\"", "").replaceFirst("\"$", "")).map { ifModifiedSince =>
      modifiedHeaderFormats.flatMap { formatter =>
        try Option(formatter.parseDateTime(ifModifiedSince))
        catch { case scala.util.control.NonFatal(e) => None }
      }.headOption.map(_.getMillis < resourceLastModified) getOrElse true
    } getOrElse true
  }

  private def compiledCodeFromClassPath(path: String, compilers: Seq[AssetCompiler]): Option[String] = {
    // try to load from class path resources
    compilers.flatMap { c =>
      c.findClassPathResource(publicBasePath, path)
        .orElse(c.findClassPathResource(basePath, path))
        .map(r => (c, r))
    }.headOption.map {
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
      val foundFile: Option[File] = Seq(
        c.findRealFile(servletContext, publicBasePath, path),
        c.findRealFile(servletContext, basePath, path)
      ).find(_.exists())

      foundFile match {
        case Some(file) => Some((c, file))
        case _ => None
      }
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
  val jsRootUrl = get(s"${jsRootPath}/*")(js).as('js)
  val cssRootUrl = get(s"${cssRootPath}/*")(css).as('css)

}

