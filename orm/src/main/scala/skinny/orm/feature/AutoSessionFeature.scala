package skinny.orm.feature

import scalikejdbc._

/**
 * Provides AutoSession for this mapper.
 */
trait AutoSessionFeature { self: ConnectionPoolFeature =>

  /**
   * AutoSession definition.
   */
  def autoSession: DBSession = {
    Option(ThreadLocalDB.load()).map { threadLocalDB =>
      if (threadLocalDB.isTxAlreadyStarted) threadLocalDB.withinTxSession()
      else threadLocalDB.autoCommitSession()
    } getOrElse {
      connectionPoolName match {
        case ConnectionPool.DEFAULT_NAME => AutoSession
        case _ => NamedAutoSession(connectionPoolName)
      }
    }
  }

}
