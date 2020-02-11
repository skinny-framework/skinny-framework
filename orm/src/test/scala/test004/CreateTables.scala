package test004

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("test004")

  addSeedSQL(
    sql"""
create table ability_type (
  id serial not null,
  name varchar(100) not null)
"""
  )
  addSeedSQL(
    sql"""
create table ability (
  id serial not null,
  name varchar(100) not null,
  ability_type_id int references ability_type(id),
  created_at timestamp not null,
  updated_at timestamp)
"""
  )
  runIfFailed(sql"select count(1) from ability_type")
}
