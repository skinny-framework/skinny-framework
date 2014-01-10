package skinny.filter

import scalikejdbc._
import skinny.controller.SkinnyController
import javax.servlet.http.HttpServletRequest

trait TxPerRequestFilter { self: SkinnyController =>

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

  before() {
    if (isDBSessionRequired(request)) {
      logger.debug(s"Thread local db session is borrowed from the pool. (connection pool: ${connectionPool.url})")
      val db = ThreadLocalDB.create(connectionPool.borrow())
      db.begin()
      logger.debug(s"Thread local db session begun. (connection pool: ${connectionPool.url})")
    }
  }

  after() {
    if (isDBSessionRequired(request)) {
      val db = ThreadLocalDB.load()
      logger.debug(s"Thread local db session is loaded. (db: ${db})")
      if (db != null) {
        try {
          db.commit()
          logger.debug(s"Thread local db session is committed. (connection pool: ${connectionPool.url})")
        } catch {
          case t: Throwable =>
            db.rollbackIfActive()
            logger.debug(s"Thread local db session is rolled back. (connection pool: ${connectionPool.url})")
            throw t
        } finally {
          try db.close()
          catch { case e: Throwable => throw e }
          logger.debug(s"Thread local db session is returned to the pool. (connection pool: ${connectionPool.url})")
        }
      }
    }
    ()
  }

}
