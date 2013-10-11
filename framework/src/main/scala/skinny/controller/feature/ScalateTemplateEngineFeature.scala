package skinny.controller.feature

import org.scalatra.scalate._
import org.fusesource.scalate.{ TemplateEngine, Binding }
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import skinny.Format

trait ScalateTemplateEngineFeature extends TemplateEngineFeature with ScalateSupport {

  override protected def defaultTemplatePath: List[String] = List("/WEB-INF/views")

  override protected def createTemplateEngine(config: ConfigT) = {
    val engine = super.createTemplateEngine(config)
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, TemplateEngine.templateTypes.map("/WEB-INF/layouts/default." + _): _*)
    engine.packagePrefix = "templates"
    engine
  }

  lazy val scalateExtension: String = "ssp"

  override protected def templatePath(path: String)(implicit format: Format = Format.HTML): String = {
    s"${path}.${format.name}.${scalateExtension}".replaceAll("//", "/")
  }

  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    findTemplate(templatePath(path)).isDefined
  }

  override protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String = {
    layoutTemplate(templatePath(path), requestScope.toMap.toSeq: _*)
  }

}
