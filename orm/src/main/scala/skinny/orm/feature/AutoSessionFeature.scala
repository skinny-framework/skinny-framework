package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._

/**
 * Provides AutoSession for this mapper.
 */
trait AutoSessionFeature { self: SQLSyntaxSupport[_] with ConnectionPoolFeature =>

  /**
   * AutoSession definition.
   */
  override def autoSession: DBSession = {
    connectionPoolName match {
      case ConnectionPool.DEFAULT_NAME =>
        Option(ThreadLocalDB.load()).map { threadLocalDB =>
          if (threadLocalDB.isTxAlreadyStarted) threadLocalDB.withinTxSession()
          else threadLocalDB.autoCommitSession()
        } getOrElse AutoSession
      case _ => NamedAutoSession(connectionPoolName)
    }
  }

}
