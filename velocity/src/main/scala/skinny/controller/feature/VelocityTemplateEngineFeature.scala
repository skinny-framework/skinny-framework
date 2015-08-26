package skinny.controller.feature

import skinny.micro.context.SkinnyContext
import skinny._
import skinny.view.velocity._

/**
 * Velocity template engine support.
 */
trait VelocityTemplateEngineFeature extends TemplateEngineFeature {

  lazy val sbtProjectPath: Option[String] = None

  lazy val velocity: Velocity =
    Velocity(VelocityViewConfig.viewWithServletContext(servletContext, sbtProjectPath))

  val velocityExtension: String = "vm"

  override protected def templatePaths(path: String)(implicit format: Format = Format.HTML): List[String] = {
    List(templatePath(path)(context, format))
  }

  protected def templatePath(path: String)(
    implicit ctx: SkinnyContext, format: Format = Format.HTML): String = {
    s"${path}.${format.name}.${velocityExtension}".replaceAll("//", "/")
  }

  override protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean = {
    velocity.templateExists(templatePath(path)(context, format))
  }

  override protected def renderWithTemplate(path: String)(
    implicit ctx: SkinnyContext, format: Format = Format.HTML): String = {
    velocity.render(templatePath(path)(ctx, format), requestScope(ctx).toMap, ctx.request, ctx.response)
  }

}
