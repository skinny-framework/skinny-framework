package test002

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("test002")

  addSeedSQL(
    sql"""
create table account (
  name varchar(128) not null)
"""
  )
  runIfFailed(sql"select count(1) from account")
}
