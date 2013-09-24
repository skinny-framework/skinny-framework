package skinny.orm.feature

import scalikejdbc._

trait ConnectionPoolFeature {

  def connectionPoolName: Any = ConnectionPool.DEFAULT_NAME

  def connectionPool: ConnectionPool = ConnectionPool(connectionPoolName)

}
