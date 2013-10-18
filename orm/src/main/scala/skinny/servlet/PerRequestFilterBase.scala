package skinny.servlet

import javax.servlet.{ FilterConfig, Filter }
import scalikejdbc.ConnectionPool
import javax.servlet.http.HttpServletRequest

trait PerRequestFilterBase extends Filter {

  def connectionPool: ConnectionPool = ConnectionPool.get()

  def only: Seq[String] = Nil

  def except: Seq[String] = Seq("/assets/?.*")

  protected def isDBSessionRequired(req: HttpServletRequest): Boolean = {
    val contextPath = req.getServletContext.getContextPath
    val path = req.getRequestURI
    val shouldBeExcluded = except.find(regexp => path.matches(s"${contextPath}${regexp}")).isDefined
    if (!shouldBeExcluded) {
      val allPathShouldBeIncluded = only.isEmpty
      val shouldBeIncluded = only.find(regexp => path.matches(s"${contextPath}${regexp}")).isDefined
      allPathShouldBeIncluded || shouldBeIncluded
    } else {
      false
    }
  }

  override def init(filterConfig: FilterConfig) {}

  override def destroy() {}

}
