package skinny.standalone

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import skinny.engine.SkinnyEngineListener
import skinny.logging.LoggerProvider

/**
 * Jetty server launcher for standalone apps.
 *
 * see: http://scalatra.org/guides/deployment/standalone.html
 */
trait JettyServer extends LoggerProvider {

  def port(port: Int): JettyServer = {
    _port = port
    this
  }

  def run() = {
    start()
    server.join
  }

  def start(): Unit = {
    refreshServer()
    logger.info(s"Starting Jetty server on port ${port}")
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
    logger.info(s"Started Jetty server on port ${port}")
  }

  def stop(): Unit = {
    server.stop()
  }

  private[this] var _port: Int = 8080

  private[this] def port: Int = {
    val port = sys.env.get("SKINNY_PORT").orElse(getEnvVarOrSysProp("skinny.port")).map(_.toInt).getOrElse(_port)
    port
  }

  private[this] def newServer: Server = new Server(port)
  private[this] def refreshServer(): Unit = server.synchronized {
    server = newServer
  }

  private[this] var server: Server = newServer

  private[this] def getEnvVarOrSysProp(key: String): Option[String] = {
    sys.env.get(key) orElse sys.props.get(key)
  }

}
