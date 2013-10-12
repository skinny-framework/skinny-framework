package skinny.orm.feature

import scalikejdbc._

/**
 * Provides AutoSession for this mapper.
 */
trait AutoSessionFeature { self: ConnectionPoolFeature =>

  /**
   * AutoSession definition.
   */
  lazy val autoSession: DBSession = connectionPoolName match {
    case ConnectionPool.DEFAULT_NAME => AutoSession
    case _ => NamedAutoSession(connectionPoolName)
  }

}
