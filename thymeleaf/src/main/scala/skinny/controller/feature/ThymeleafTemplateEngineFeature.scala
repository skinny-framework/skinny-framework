package skinny.controller.feature

import skinny._
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver._
import org.thymeleaf.context.WebContext
import scala.collection.JavaConverters._

/**
 * Thymeleaf template engine support.
 *
 * @see http://www.thymeleaf.org/
 */
trait ThymeleafTemplateEngineFeature extends TemplateEngineFeature {

  /**
   * Set as cacheable if true.
   */
  lazy val thymeleafCacheable: Boolean = !isDevelopmentMode

  /**
   * Default template mode.
   */
  lazy val thymeleafResolverTemplateMode: String = "LEGACYHTML5"

  /**
   * Resolver template prefix.
   */
  lazy val thymeleafResolverPrefix: String = {
    if (SkinnyEnv.isTest()) "WEB-INF/views/"
    else "/WEB-INF/views/"
  }

  /**
   * Resolver template suffix.
   */
  lazy val thymeleafResolverSuffix: String = ".html"

  /**
   * Template cache ttl milliseconds.
   */
  lazy val thymeleafResolverCacheTTLMs: Long = 3600000L

  /**
   * Resolver.
   */
  lazy val thymeleafResolver: TemplateResolver = {
    val resolver: TemplateResolver = {
      if (SkinnyEnv.isTest()) new ClassLoaderTemplateResolver
      else new ServletContextTemplateResolver
    }
    resolver.setCacheable(thymeleafCacheable)
    resolver.setTemplateMode(thymeleafResolverTemplateMode)
    resolver.setPrefix(thymeleafResolverPrefix)
    resolver.setSuffix(thymeleafResolverSuffix)
    resolver.setCharacterEncoding(charset.getOrElse("UTF-8"))
    resolver.setCacheTTLMs(thymeleafResolverCacheTTLMs)
    resolver
  }

  /**
   * Template engine.
   */
  lazy val thymeleafTemplateEngine: TemplateEngine = {
    val engine = new TemplateEngine
    engine.setTemplateResolver(thymeleafResolver)
    engine
  }

  override protected def templatePaths(path: String)(implicit format: Format = Format.HTML): List[String] = {
    List(templatePath(path))
  }

  protected def templatePath(path: String)(implicit format: Format = Format.HTML): String = {
    s"${path}".replaceAll("//", "/")
  }

  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    // only HTML template is supported
    format == Format.HTML
  }

  override protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String = {
    val context = new WebContext(request, response, servletContext)
    requestScope().foreach {
      case (key, value: Map[_, _]) => context.setVariable(key, value.asJava)
      case (key, value: Iterable[_]) => context.setVariable(key, value.asJava)
      case (key, value) => context.setVariable(key, value)
    }
    thymeleafTemplateEngine.process(templatePath(path), context)
  }

}
