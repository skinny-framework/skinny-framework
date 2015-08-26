package skinny.controller.feature

import skinny.micro.context.SkinnyContext
import skinny.Format
import java.io.IOException
import skinny.view.freemarker._

/**
 * FreeMarker template engine support.
 */
trait FreeMarkerTemplateEngineFeature extends TemplateEngineFeature {

  lazy val sbtProjectPath: Option[String] = None

  /**
   * FreeMarker Scala wrapper.
   */
  lazy val freeMarker: FreeMarker = {
    FreeMarker(FreeMarkerConfig.defaultWithServletContext(servletContext, sbtProjectPath))
  }

  lazy val freeMarkerExtension = "ftl"

  override protected def templatePaths(path: String)(implicit format: Format = Format.HTML): List[String] = {
    List(templatePath(path))
  }

  protected def templatePath(path: String)(implicit format: Format = Format.HTML): String = {
    s"${path}.${format.name}.${freeMarkerExtension}".replaceAll("//", "/")
  }

  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    try {
      freeMarker.config.getTemplate(templatePath(path)) != null
    } catch {
      case e: IOException =>
        logger.error("Failed to find template", e)
        false
    }
  }

  override protected def renderWithTemplate(path: String)(
    implicit ctx: SkinnyContext, format: Format = Format.HTML): String = {
    freeMarker.render(templatePath(path), requestScope(ctx).toMap)
  }

}
