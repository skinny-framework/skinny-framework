package sysadmin

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("sysadmin")

  addSeedSQL(
    sql"""
create table user (
  id bigint auto_increment primary key not null,
  os varchar(128) not null,
  java varchar(128) not null,
  user varchar(128) not null
)
"""
  )

  runIfFailed(sql"select count(1) from user")
}
