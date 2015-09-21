package skinny.http.test

import org.eclipse.jetty.server.Server
import skinny.http.test.server.HttpServer

trait ServerOps {

  def newServer(port: Int) = new Server(port)

  def withServer[A](port: Int)(f: (Server) => A): A = withServer(newServer(port))(f)

  def withServer[A](server: Server)(f: (Server) => A): A = {
    try f(server)
    finally {
      server.stop()
      Thread.sleep(300L)
    }
  }

  def withServer[A](server: HttpServer)(f: (HttpServer) => A): A = {
    try f(server)
    finally {
      server.stop()
      Thread.sleep(300L)
    }
  }

  def runnable(server: Server) = new Runnable() {
    def run() {
      try server.start()
      catch { case e: Throwable => e.printStackTrace() }
    }
  }

  def start(server: Server) = {
    new Thread(runnable(server)).start()
    Thread.sleep(800L)
  }

  def start(server: HttpServer) = {
    new Thread(new Runnable() {
      def run() {
        server.start()
      }
    }).start()
    Thread.sleep(800L)
  }

}
