package skinny.controller.feature

import skinny.Format
import java.io.IOException
import skinny.view.freemarker._

/**
 * FreeMarker template engine support.
 */
trait FreeMarkerTemplateEngineFeature extends TemplateEngineFeature {

  /**
   * FreeMarker Scala wrapper.
   */
  lazy val freeMarker: FreeMarker = FreeMarker(FreeMarkerConfig.defaultWithServletContext(servletContext))

  override protected def templatePaths(path: String)(implicit format: Format = Format.HTML): List[String] = {
    List(templatePath(path))
  }

  protected def templatePath(path: String)(implicit format: Format = Format.HTML): String = {
    s"${path}.${format.name}.ftl".replaceAll("//", "/")
  }

  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    try {
      freeMarker.config.getTemplate(templatePath(path)) != null
    } catch { case e: IOException => false }
  }

  override protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String = {
    freeMarker.render(templatePath(path), requestScope.toMap)
  }

}
