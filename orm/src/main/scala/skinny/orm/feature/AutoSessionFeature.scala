package skinny.orm.feature

import scalikejdbc._

trait AutoSessionFeature { self: ConnectionPoolFeature =>

  val autoSession: DBSession = connectionPoolName match {
    case ConnectionPool.DEFAULT_NAME => AutoSession
    case _ => NamedAutoSession(connectionPoolName)
  }

}
