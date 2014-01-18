package skinny.filter

import scalikejdbc._
import javax.servlet.http.HttpServletRequest
import grizzled.slf4j.Logging

/**
 * A filter which enables controller wired with a single transactional DB session.
 */
trait TxPerRequestFilter extends SkinnyFilter with Logging {

  def dbConnectionPool: ConnectionPool = ConnectionPool.get()

  def beginDBConnection = {
    val db = ThreadLocalDB.create(dbConnectionPool.borrow())
    logger.debug(s"Thread local db session is borrowed from the pool. (db: ${db})")
    db.begin()
    logger.debug(s"Thread local db session begun. (db: ${db})")
  }

  addErrorFilter {
    case e: Throwable =>
      Option(ThreadLocalDB.load()).map { db =>
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
      }.getOrElse {
        logger.debug("Thread local db session is not found.")
      }
  }

  def commitDBConnection = {
    val db = ThreadLocalDB.load()
    if (db != null && !db.isTxNotActive) {
      val info = db.toString
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

  beforeAction()(beginDBConnection)

  afterAction()(commitDBConnection)

}
