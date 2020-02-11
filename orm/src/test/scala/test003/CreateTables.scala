package test003

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("test003")

  addSeedSQL(
    sql"""
create table person (
  id serial not null,
  name varchar(100) not null)
"""
  )
  addSeedSQL(
    sql"""
create table company (
  id serial not null,
  name varchar(100) not null)
"""
  )
  addSeedSQL(
    sql"""
create table employee (
  company_id int not null,
  person_id int not null,
  role varchar(100),
  primary key(company_id, person_id))
"""
  )
  runIfFailed(sql"select count(1) from employee")
}
