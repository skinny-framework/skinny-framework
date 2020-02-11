package sysadmin

import scalikejdbc._
import skinny.orm._

case class User(id: Long, os: String, java: String, user: String)

object User extends SkinnyCRUDMapper[User] {

  override val connectionPoolName = "sysadmin"
  override def defaultAlias       = createAlias("u")

  override def extract(rs: WrappedResultSet, n: ResultName[User]): User = new User(
    rs.get(n.id),
    rs.get(n.os),
    rs.get(n.java),
    rs.get(n.user)
  )
}
