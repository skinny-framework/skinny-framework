package test002

import skinny.orm._
import scalikejdbc._

case class Account(name: String)

object Account extends SkinnyNoIdCRUDMapper[Account] {
  override def connectionPoolName = "test002"

  override def tableName                                                      = "account"
  override def defaultAlias: Alias[Account]                                   = createAlias("a")
  override def extract(rs: WrappedResultSet, n: ResultName[Account]): Account = new Account(rs.get(n.name))
}
