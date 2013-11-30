package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._

/**
 * Provides ConnectionPool.
 */
trait ConnectionPoolFeature { self: SQLSyntaxSupport[_] =>

  /**
   * Returns connection pool.
   *
   * @return pool
   */
  def connectionPool: ConnectionPool = ConnectionPool(connectionPoolName)

}
