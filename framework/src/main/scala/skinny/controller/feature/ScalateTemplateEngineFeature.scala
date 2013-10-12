package skinny.controller.feature

import org.scalatra.scalate._
import org.fusesource.scalate.{ TemplateEngine, Binding }
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import skinny.Format

/**
 * Scalate implementation of TemplateEngineSupport.
 *
 * This is basically same as Scalatra's Scalate support, but the convention of template file path is inspired by Ruby on Rails.
 *
 * {{{
 *   render("/members/index")
 * }}}
 *
 * The above code expects "src/main/webapp/WEB-INF/views/members/index.html.ssp" by default.
 *
 * If you need to use scaml instead,
 *
 * {{{
 *   override lazy val scalateExtension: String = "scaml"
 * }}}
 *
 * And then, Skinny expects "src/main/webapp/WEB-INF/views/members/index.html.scaml"
 */
trait ScalateTemplateEngineFeature extends TemplateEngineFeature with ScalateSupport {

  /**
   * Overrides to make the template path simpler.
   *
   * @return paths
   */
  override protected def defaultTemplatePath: List[String] = List("/WEB-INF/views")

  /**
   * Creates TemplateEngine instance for Skinny app.
   *
   * @param config configuration
   * @return TemplateEngine instance
   */
  override protected def createTemplateEngine(config: ConfigT) = {
    val engine = super.createTemplateEngine(config)
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, TemplateEngine.templateTypes.map("/WEB-INF/layouts/default." + _): _*)
    engine.packagePrefix = "templates"
    engine
  }

  /**
   * Scalate extension name. If you'd like to use another one, override this attribute.
   */
  lazy val scalateExtension: String = "ssp"

  /**
   * Returns the actual template path for the name.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return actual path
   */
  override protected def templatePath(path: String)(implicit format: Format = Format.HTML): String = {
    s"${path}.${format.name}.${scalateExtension}".replaceAll("//", "/")
  }

  /**
   * Predicates the template path is available.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return true/false
   */
  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    findTemplate(templatePath(path)).isDefined
  }

  /**
   * Renders body with template.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return true/false
   */
  override protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String = {
    layoutTemplate(templatePath(path), requestScope.toMap.toSeq: _*)
  }

}
