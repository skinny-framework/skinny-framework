package skinny.filter

import scalikejdbc._
import skinny.logging.LoggerProvider

/**
 * A filter which enables controller wired with a single transactional DB session.
 */
trait TxPerRequestFilter extends SkinnyFilter with LoggerProvider {

  beforeAction()(beginTxPerRequest)

  afterAction()(commitTxPerRequest)

  addErrorFilter {
    case scala.util.control.NonFatal(e) => rollbackTxPerRequest
  }

  def connectionPoolForTxPerRequestFilter: ConnectionPool = ConnectionPool.get()

  def beginTxPerRequest = {
    val db = ThreadLocalDB.create(connectionPoolForTxPerRequestFilter.borrow())
    logger.debug(s"Thread local db session is borrowed from the pool. (db: ${db})")
    db.begin()
    logger.debug(s"Thread local db session begun. (db: ${db})")
  }

  def rollbackTxPerRequest = {
    Option(ThreadLocalDB.load()).map { db =>
      if (!db.isTxNotActive) {
        logger.debug(s"Thread local db session is loaded. (db: ${db})")
        try {
          db.rollbackIfActive()
          logger.debug(s"Thread local db session is rolled back. (db: ${db})")
        } finally {
          try db.close()
          catch { case scala.util.control.NonFatal(_) => }
          logger.debug(s"Thread local db session is returned to the pool. (db: ${db})")
        }
      } else {
        logger.debug("Thread local session is already inactive. (db: ${db})")
      }
    }.getOrElse {
      logger.debug("Thread local db session is not found.")
    }
  }

  def commitTxPerRequest = {
    Option(ThreadLocalDB.load()).map { db =>
      if (!db.isTxNotActive) {
        logger.debug(s"Thread local db session is loaded. (db: ${db})")
        try {
          db.commit()
          logger.debug(s"Thread local db session is committed. (db: ${db})")
        } finally {
          try db.close()
          catch { case scala.util.control.NonFatal(_) => }
          logger.debug(s"Thread local db session is returned to the pool. (db: ${db})")
        }
      } else {
        logger.debug("Thread local session is already inactive. (db: ${db})")
      }
    }.getOrElse {
      logger.debug("Thread local db session is not found.")
    }
  }

}
