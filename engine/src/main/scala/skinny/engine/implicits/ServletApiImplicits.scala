package skinny.engine.implicits

import scala.language.implicitConversions

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse, HttpSession }

import skinny.engine.context.RichServletContext
import skinny.engine.request.{ RichHttpServletSession, RichRequest }
import skinny.engine.response.RichResponse

trait ServletApiImplicits {

  implicit def enrichRequest(request: HttpServletRequest): RichRequest =
    RichRequest(request)

  implicit def enrichResponse(response: HttpServletResponse): RichResponse =
    RichResponse(response)

  implicit def enrichSession(session: HttpSession): RichHttpServletSession =
    RichHttpServletSession(session)

  implicit def enrichServletContext(servletContext: ServletContext): RichServletContext =
    RichServletContext(servletContext)

}

object ServletApiImplicits
  extends ServletApiImplicits
