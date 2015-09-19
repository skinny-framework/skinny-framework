package skinny.http.test.server

import java.io.File
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.apache.commons.fileupload.{FileItem, FileUploadException}
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}

class PostFormdataServer(val port: Int = 8888, behavior: ServerBehavior = PostFormdataServer.defaultDoPost) extends HttpServer {

    lazy val handler: ServletContextHandler  = new ServletContextHandler()

    class FileuploadServlet extends HttpServlet {

        private val serialVersionUID : Long = 1L

        override def doPost(req: HttpServletRequest, resp: HttpServletResponse) = behavior(req, resp)
    }

    private var server: Server = null

    override def start() {
        handler.addServlet(new ServletHolder(new FileuploadServlet()), "/*")
        server = new Server(this.port)
        server.setHandler(handler)
        server.start()
        server.join()
    }

    override def stop() {
        server.stop()
    }
}
object PostFormdataServer {
//    @SuppressWarnings("unchecked")
    private val defaultDoPost = { (req: HttpServletRequest, resp: HttpServletResponse) =>
          if (ServletFileUpload.isMultipartContent(req)) {

              val factory = new DiskFileItemFactory()
              factory.setSizeThreshold(1426)
              factory.setRepository(new File("target"))

              val upload = new ServletFileUpload(factory)
              upload.setSizeMax(20 * 1024)
              upload.setFileSizeMax(10 * 1024)

              var toResponse = "FAILURE!!!"
              try {
                  import scala.collection.JavaConverters._
                  val items: java.util.List[FileItem] = upload.parseRequest(req)
                  for (item <- items.asScala) {
                      if ("toResponse".equals(item.getFieldName())) {
                          toResponse = item.getString()
                      }
                      val result = item.getFieldName() + "(" + item.getContentType() + ") -> " + item.getString("UTF-8");
                      //System.out.println(result);
                  }
              } catch  {
                  case e: FileUploadException =>
                      e.printStackTrace()
              }
              resp.getWriter().write(toResponse)
        }
    }
}
