package skinny.controller.feature

import org.scalatra.scalate._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import skinny._
import javax.servlet.http.HttpServletRequest

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
trait ScalateTemplateEngineFeature extends TemplateEngineFeature
    with ScalateSupport
    with ScalateUrlGeneratorSupport {

  /**
   * To deal with exceptions.
   *
   * http://www.scalatra.org/guides/views/scalate.html#toc_303
   */
  override def isScalateErrorPageEnabled = false

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
  def scalateExtension: String = "ssp"

  /**
   * Returns the actual template path for the name.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return actual path
   */
  override protected def templatePath(path: String)(implicit format: Format = Format.HTML): String = {
    s"${path}.${format.name}.${scalateExtension}".replaceAll("//", "/").replaceFirst("^/", "")
  }

  /**
   * Predicates the template path is available.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return true/false
   */
  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    val exists = findTemplate(templatePath(path)).isDefined
    if ((SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()) && !exists && format == Format.HTML) {
      generateWelcomePageIfAbsent(path)
      true
    } else {
      exists
    }
  }

  /**
   * Generates a sample page for absent page.
   */
  protected def generateWelcomePageIfAbsent(path: String)(implicit format: Format = Format.HTML): Unit = {
    import org.apache.commons.io.FileUtils
    import java.io.File
    val filePath = servletContext.getRealPath(s"/WEB-INF/views/${templatePath(path)}")
    val file = new File(filePath)
    val code: String = scalateExtension match {
      case "jade" =>
        """h3 Welcome
          |hr
          |p(class="alert alert-success")
          |  b TODO:
          |  | This is an auto-generated file by
          |  a(href="http://skinny-framework.org/") Skinny framework
          |  |!
          |""".stripMargin
      case "scaml" =>
        """%h3 Welcome
          |%hr
          |p(class="alert alert-success")
          |  %b TODO:
          |  This is an auto-generated file by
          |  %a(href="http://skinny-framework.org/") Skinny framework
          |  !
          |""".stripMargin
      case _ =>
        """<h3>Welcome</h3>
          |<hr/>
          |<p class="alert alert-success">
          |  <strong>TODO:</strong> This is an auto-generated file by <a href="http://skinny-framework.org/">Skinny framework</a>!<br/>
          |</p>
          |""".stripMargin
    }
    FileUtils.write(file, code)
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

  /**
   * Replaces layout template for this action.
   */
  def layout(path: String)(implicit request: HttpServletRequest): ScalateTemplateEngineFeature = {
    if (request != null) {
      val _path = path.replaceFirst("^/", "").replaceAll("//", "/").replaceFirst("/$", "")
      templateAttributes += ("layout" -> s"/WEB-INF/layouts/${_path}.${scalateExtension}")
      this
    } else {
      // TODO this method doesn't work in beforeAction, more investigation
      logger.warn("You cannot specify layout here. Put this into action methods.")
      this
    }
  }

}
