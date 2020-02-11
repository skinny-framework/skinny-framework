package test001

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("test001")

  addSeedSQL(
    sql"""
create table test1 (
  id bigint serial not null,
  name varchar(128) not null)
""",
    sql"""
create table test2 (
  id bigint serial not null,
  name varchar(128) not null)
""",
    sql"""
create table test1_test2 (
  test1_id bigint not null,
  test2_id bigint not null)
"""
  )

  runIfFailed(sql"select count(1) from test1")
}
