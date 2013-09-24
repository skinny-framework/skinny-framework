package skinny.controller.feature

import skinny.Format

trait ScalateTemplateEngineFeature extends TemplateEngineFeature {

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
