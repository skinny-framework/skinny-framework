package skinny.servlet

import scalikejdbc._
import javax.servlet._
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

class DBSessionPerRequestFilter extends PerRequestFilterBase {

  private[this] val logger = LoggerFactory.getLogger(classOf[DBSessionPerRequestFilter])

  override def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) = {
    if (isDBSessionRequired(req.asInstanceOf[HttpServletRequest])) {
      try {
        using(connectionPool.borrow()) { conn =>
          logger.debug(s"Thread local db session is borrowed from the pool. (connection pool: ${connectionPool.url})")
          ThreadLocalDB.create(conn)
          chain.doFilter(req, res)
        }
      } finally {
        logger.debug(s"Thread local db session is returned to the pool. (connection pool: ${connectionPool.url})")
      }
    } else {
      chain.doFilter(req, res)
    }
  }

}
