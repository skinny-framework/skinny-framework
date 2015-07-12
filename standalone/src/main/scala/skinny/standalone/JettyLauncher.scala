package skinny.standalone

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import skinny.engine.SkinnyEngineListener

/**
 * Jetty server launcher for standalone apps.
 *
 * see: http://scalatra.org/2.2/guides/deployment/standalone.html
 */
object JettyLauncher {

  def main(args: Array[String]) {
    val port = sys.env.get("SKINNY_PORT").orElse(getEnvVarOrSysProp("skinny.port")).map(_.toInt).getOrElse(8080)
    println(s"Starting Jetty on port ${port}")

    val server = new Server(port)
    val context = new WebAppContext()
    val contextPath = sys.env.get("SKINNY_PREFIX").orElse(getEnvVarOrSysProp("skinny.prefix")).getOrElse("/")
    context.setContextPath(contextPath)
    context.setWar({
      val domain = this.getClass.getProtectionDomain
      val location = domain.getCodeSource.getLocation
      location.toExternalForm
    })
    context.addEventListener(new SkinnyEngineListener)
    context.addServlet(classOf[DefaultServlet], "/")
    server.setHandler(context)
    server.start
    server.join
  }

  def getEnvVarOrSysProp(key: String): Option[String] = {
    sys.env.get(key) orElse sys.props.get(key)
  }

}

