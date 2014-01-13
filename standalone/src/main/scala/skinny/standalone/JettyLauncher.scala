package skinny.standalone

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ DefaultServlet, ServletContextHandler }
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
 * Jetty server launcher for standalone apps.
 *
 * see: http://scalatra.org/2.2/guides/deployment/standalone.html
 */
object JettyLauncher {

  def main(args: Array[String]) {
    val port = if (System.getenv("skinny.port") != null) System.getenv("skinny.port").toInt else 8080
    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")
    server.setHandler(context)
    server.start
    server.join
  }

}

