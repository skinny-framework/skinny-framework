package skinny.filter

import scalikejdbc._
import javax.servlet.http.HttpServletRequest
import grizzled.slf4j.Logging

/**
 * A filter which enables controller wired with a single transactional DB session.
 */
trait TxPerRequestFilter extends SkinnyFilter with Logging { self: SkinnyFilterActivation =>

  def dbConnectionPool: ConnectionPool = ConnectionPool.get()

  def txOnly: Seq[String] = Nil

  def txExcept: Seq[String] = Seq("/assets/?.*")

  protected def isDBSessionRequired(req: HttpServletRequest): Boolean = {
    if (req == null || req.getServletContext == null) {
      false
    } else {
      val contextPath = req.getServletContext.getContextPath
      val path = req.getRequestURI
      val shouldBeExcluded = txExcept.find(regexp => path.matches(s"${contextPath}${regexp}")).isDefined
      if (!shouldBeExcluded) {
        val allPathShouldBeIncluded = txOnly.isEmpty
        val shouldBeIncluded = txOnly.find(regexp => path.matches(s"${contextPath}${regexp}")).isDefined
        allPathShouldBeIncluded || shouldBeIncluded
      } else {
        false
      }
    }
  }

  def beginDBConnection = {
    if (isDBSessionRequired(request)) {
      val db = ThreadLocalDB.create(dbConnectionPool.borrow())
      logger.debug(s"Thread local db session is borrowed from the pool. (db: ${db})")
      db.begin()
      logger.debug(s"Thread local db session begun. (db: ${db})")
    }
  }

  addErrorFilter {
    case e: Throwable =>
      val db = ThreadLocalDB.load()
      val info = db.toString
      if (db != null && !db.isTxNotActive) {
        logger.debug(s"Thread local db session is loaded. (db: ${info})")
        try {
          db.rollbackIfActive()
          logger.debug(s"Thread local db session is rolled back. (db: ${info})")
        } finally {
          try db.close()
          catch { case e: Exception => }
          logger.debug(s"Thread local db session is returned to the pool. (db: ${info})")
        }
      }
  }

  def commitDBConnection() = {
    if (isDBSessionRequired(request)) {
      val db = ThreadLocalDB.load()
      val info = db.toString
      if (db != null && !db.isTxNotActive) {
        logger.debug(s"Thread local db session is loaded. (db: ${info})")
        try {
          db.commit()
          logger.debug(s"Thread local db session is committed. (db: ${info})")
        } finally {
          try db.close()
          catch { case e: Exception => }
          logger.debug(s"Thread local db session is returned to the pool. (db: ${info})")
        }
      }
    }
  }

  beforeAction()(beginDBConnection)

  afterAction()(commitDBConnection)

}
