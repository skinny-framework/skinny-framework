package skinny.http

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{ Request => BaseRequest }
import javax.servlet.http._
import server.handler.MethodHandler

trait Handlers {

  val getHandler = new AbstractHandler {
    def handle(target: String, baseReq: BaseRequest, req: HttpServletRequest, resp: HttpServletResponse) = {
      try {
        if (req.getMethod().equals("GET")) {
          val foo = req.getParameter("foo")
          val bar = req.getParameter("bar")
          val result = "foo:" + foo + (if (bar == null) "" else ",bar:" + bar)
          resp.setCharacterEncoding("UTF-8")
          resp.getWriter().print(result)
          baseReq.setHandled(true)
          resp.setStatus(HttpServletResponse.SC_OK)
        } else {
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN)
        }
      } catch { case e: Throwable => e.printStackTrace() }
    }
  }

  val postHandler = new AbstractHandler {
    def handle(target: String, baseReq: BaseRequest, req: HttpServletRequest, resp: HttpServletResponse) = {
      try {
        if (req.getMethod().equals("POST")) {
          val foo = req.getParameter("foo")
          val result = "foo:" + foo
          resp.setCharacterEncoding("UTF-8")
          resp.getWriter().print(result)
          baseReq.setHandled(true)
          resp.setStatus(HttpServletResponse.SC_OK)
        } else {
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN)
        }
      } catch { case e: Throwable => e.printStackTrace() }
    }
  }

  val putHandler = new AbstractHandler {
    def handle(target: String, baseReq: BaseRequest, req: HttpServletRequest, resp: HttpServletResponse) = {
      try {
        if (req.getMethod().equals("PUT")) {
          val foo = req.getParameter("foo")
          val result = "foo:" + foo
          resp.setCharacterEncoding("UTF-8")
          resp.getWriter().print(result)
          baseReq.setHandled(true)
          resp.setStatus(HttpServletResponse.SC_OK)
        } else {
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN)
        }
      } catch { case e: Throwable => e.printStackTrace() }
    }
  }

  val deleteHandler = new MethodHandler {
    override def getMethod: Method = Method.DELETE
  }

  val traceHandler = new MethodHandler {
    override def getMethod: Method = Method.TRACE
  }

  val optionsHandler = new MethodHandler {
    override def getMethod: Method = Method.OPTIONS
  }

}
