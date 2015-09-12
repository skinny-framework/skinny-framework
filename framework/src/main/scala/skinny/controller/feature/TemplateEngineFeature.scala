package skinny.controller.feature

import skinny.Format
import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.json4s.JSONSupport
import skinny.micro.response.ResponseStatus
import skinny.logging.LoggerProvider
import skinny.exception.ViewTemplateNotFoundException

/**
 * TemplateEngine support for Skinny app.
 */
trait TemplateEngineFeature
    extends SkinnyControllerCommonBase
    with RequestScopeFeature
    with JSONSupport
    with LoggerProvider {

  /**
   * Renders body with template.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return body
   */
  def render(path: String)(implicit ctx: SkinnyContext, format: Format = Format.HTML): String = {
    setContentTypeIfAbsent()(format)

    if (templateExists(path)(format)) {
      // template found, render with it
      renderWithTemplate(path)(ctx, format)
    } else if (format == Format.HTML) {
      // template not found and should be found
      throw new ViewTemplateNotFoundException(s"View template not found. (expected: one of ${templatePaths(path)})")
    } else {
      // template not found, but try to render JSON or XML body if possible
      logger.debug(s"Template for ${path} not found (format: ${format}).")
      val entity = (for {
        resourcesName <- getFromRequestScope[String](RequestScopeFeature.ATTR_RESOURCES_NAME)(ctx)
        resources <- getFromRequestScope[Any](resourcesName)(ctx)
      } yield resources) getOrElse {
        for {
          resourceName <- getFromRequestScope[String](RequestScopeFeature.ATTR_RESOURCE_NAME)(ctx)
          resource <- getFromRequestScope[Any](resourceName)(ctx)
        } yield resource
      }
      // renderWithFormat returns null when body is empty
      Option(renderWithFormat(entity)(format)).getOrElse(haltWithBody(404)(ctx, format))
    }
  }

  /**
   * Returns possible template paths.
   * Result is a list because the template engine may support multiple template languages.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return actual path
   */
  protected def templatePaths(path: String)(implicit format: Format = Format.HTML): List[String]

  /**
   * Predicates the template exists.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return true/false
   */
  protected def templateExists(path: String)(implicit format: Format = Format.HTML): Boolean

  /**
   * Renders with template.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return body
   */
  protected def renderWithTemplate(path: String)(
    implicit ctx: SkinnyContext, format: Format = Format.HTML): String

  override protected def haltWithBody[A](httpStatus: Int)(
    implicit ctx: SkinnyContext, format: Format = Format.HTML): A = {
    val body: String = format match {
      case Format.HTML => render(s"/error/${httpStatus}")(ctx, format)
      case _ => renderWithFormat(Map("status" -> httpStatus, "message" -> ResponseStatus(httpStatus).message))(format)
    }
    Option(body).map { b =>
      halt(status = httpStatus, body = b)
    }.getOrElse {
      halt(status = httpStatus)
    }
  }

}
