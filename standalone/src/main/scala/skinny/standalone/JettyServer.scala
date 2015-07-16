package skinny.standalone

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import skinny.engine.SkinnyEngineListener

/**
 * Jetty server launcher for standalone apps.
 *
 * see: http://scalatra.org/guides/deployment/standalone.html
 */
trait JettyServer {

  def port(port: Int): JettyServer = {
    _port = port
    this
  }

  def run() = {
    start()
    server.join
  }

  def start(): Unit = {
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
  }

  def stop(): Unit = {
    server.stop()
  }

  private[this] var _port: Int = 8080

  private[this] lazy val port: Int = {
    val port = sys.env.get("SKINNY_PORT").orElse(getEnvVarOrSysProp("skinny.port")).map(_.toInt).getOrElse(_port)
    println(s"Starting Jetty on port ${port}")
    port
  }

  private[this] lazy val server: Server = new Server(port)

  private[this] def getEnvVarOrSysProp(key: String): Option[String] = {
    sys.env.get(key) orElse sys.props.get(key)
  }

}
