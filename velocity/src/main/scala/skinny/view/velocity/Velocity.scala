package skinny.view.velocity

import java.io.StringWriter
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.apache.velocity.tools.view.VelocityView

/**
  * Velocity renderer.
  *
  * @param velocityView VelocityView
  */
case class Velocity(velocityView: VelocityView) {

  def render(path: String,
             values: Map[String, Any],
             request: HttpServletRequest,
             response: HttpServletResponse): String = {
    val context = velocityView.createContext(request, response)
    values.foreach { case (k, v) => context.put(k, v) }
    val template = velocityView.getTemplate(path)
    val writer   = new StringWriter
    velocityView.merge(template, context, writer)
    writer.toString
  }

  def templateExists(path: String): Boolean = velocityView.getVelocityEngine.resourceExists(path)

}
