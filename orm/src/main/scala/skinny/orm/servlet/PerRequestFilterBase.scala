package skinny.orm.servlet

import javax.servlet.{ Filter, FilterConfig }
import scalikejdbc.ConnectionPool
import javax.servlet.http.HttpServletRequest

trait PerRequestFilterBase extends Filter {

  def connectionPool: ConnectionPool = ConnectionPool.get()

  def only: Seq[String] = Nil

  def except: Seq[String] = Seq("/assets/?.*")

  protected def isDBSessionRequired(req: HttpServletRequest): Boolean = {
    val contextPath      = req.getServletContext.getContextPath
    val path             = req.getRequestURI
    val shouldBeExcluded = except.exists(regexp => path.matches(s"${contextPath}${regexp}"))
    if (!shouldBeExcluded) {
      val allPathShouldBeIncluded = only.isEmpty
      val shouldBeIncluded        = only.exists(regexp => path.matches(s"${contextPath}${regexp}"))
      allPathShouldBeIncluded || shouldBeIncluded
    } else {
      false
    }
  }

  override def init(filterConfig: FilterConfig): Unit = {}

  override def destroy(): Unit = {}

}
