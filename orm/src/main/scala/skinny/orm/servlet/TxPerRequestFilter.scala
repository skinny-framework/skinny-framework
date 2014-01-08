package skinny.orm.servlet

import scalikejdbc._
import javax.servlet._
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

class TxPerRequestFilter extends PerRequestFilterBase {

  private[this] val logger = LoggerFactory.getLogger(classOf[TxPerRequestFilter])

  override def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) = {
    if (isDBSessionRequired(req.asInstanceOf[HttpServletRequest])) {
      using(connectionPool.borrow()) { conn =>
        logger.debug(s"Thread local db session is borrowed from the pool. (connection pool: ${connectionPool.url})")
        val db = ThreadLocalDB.create(conn)
        try {
          db.begin()
          logger.debug(s"Thread local db session begun. (connection pool: ${connectionPool.url})")
          chain.doFilter(req, res)
          db.commit()
          logger.debug(s"Thread local db session is committed. (connection pool: ${connectionPool.url})")
        } catch {
          case t: Throwable =>
            db.rollbackIfActive()
            logger.debug(s"Thread local db session is rolled back. (connection pool: ${connectionPool.url})")
            throw t
        } finally {
          logger.debug(s"Thread local db session is returned to the pool. (connection pool: ${connectionPool.url})")
        }
      }
    } else {
      chain.doFilter(req, res)
    }
  }

}
