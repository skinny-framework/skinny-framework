package skinny.engine

import skinny.standalone.JettyServer

import scala.collection.mutable.ListBuffer

trait WebServer extends JettyServer {

  private[this] val registeredWebAppHandlers = new ListBuffer[Handler]

  def mountableHandlers: Seq[Handler] = registeredWebAppHandlers.toSeq

  def mount(handler: Handler): WebServer = {
    registeredWebAppHandlers.append(handler)
    this
  }

  override def start(): Unit = {
    WebServer.singleton = this
    super.start()
  }

}

object WebServer extends WebServer {

  var singleton: WebServer = this

}
