package skinny.orm.feature

import scalikejdbc._

/**
 * Provides ConnectionPool.
 */
trait ConnectionPoolFeature {

  /**
   * Returns connection pool name.
   *
   * @return name
   */
  def connectionPoolName: Any = ConnectionPool.DEFAULT_NAME

  /**
   * Returns connection pool.
   *
   * @return pool
   */
  def connectionPool: ConnectionPool = ConnectionPool(connectionPoolName)

}
