package skinny.controller.feature

import skinny.Format
import skinny.engine.response.ResponseStatus
import skinny.logging.LoggerProvider
import skinny.exception.ViewTemplateNotFoundException
import skinny.controller.SkinnyControllerBase

/**
 * TemplateEngine support for Skinny app.
 */
trait TemplateEngineFeature
    extends SkinnyControllerBase
    with RequestScopeFeature
    with JSONFeature
    with LoggerProvider {

  /**
   * Renders body with template.
   *
   * @param path path name
   * @param format format (HTML,JSON,XML...)
   * @return body
   */
  def render(path: String)(implicit format: Format = Format.HTML): String = {
    setContentTypeIfAbsent()

    if (templateExists(path)) {
      // template found, render with it
      renderWithTemplate(path)
    } else if (format == Format.HTML) {
      // template not found and should be found
      throw new ViewTemplateNotFoundException(s"View template not found. (expected: one of ${templatePaths(path)})")
    } else {
      // template not found, but try to render JSON or XML body if possible
      logger.debug(s"Template for ${path} not found (format: ${format}).")
      val entity = (for {
        resourcesName <- getFromRequestScope[String](RequestScopeFeature.ATTR_RESOURCES_NAME)
        resources <- getFromRequestScope[Any](resourcesName)
      } yield resources) getOrElse {
        for {
          resourceName <- getFromRequestScope[String](RequestScopeFeature.ATTR_RESOURCE_NAME)
          resource <- getFromRequestScope[Any](resourceName)
        } yield resource
      }
      // renderWithFormat returns null when body is empty
      Option(renderWithFormat(entity)).getOrElse(haltWithBody(404))
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
  protected def renderWithTemplate(path: String)(implicit format: Format = Format.HTML): String

  override protected def haltWithBody[A](httpStatus: Int)(implicit format: Format = Format.HTML): A = {
    val body: String = format match {
      case Format.HTML => render(s"/error/${httpStatus}")
      case _ => renderWithFormat(Map("status" -> httpStatus, "message" -> ResponseStatus(httpStatus).message))
    }
    Option(body).map { b =>
      halt(status = httpStatus, body = b)
    }.getOrElse {
      halt(status = httpStatus)
    }
  }

}
