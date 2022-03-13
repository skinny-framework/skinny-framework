package service

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("service")

  addSeedSQL(
    sql"""
create table services (
  no bigint auto_increment primary key not null,
  name varchar(128) not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  deleted_at timestamp
)
""",
    sql"""
create table applications (
  id bigint auto_increment primary key not null,
  name varchar(128) not null,
  service_no bigint not null references services(no),
  created_at timestamp not null,
  updated_at timestamp not null,
  deleted_at timestamp
)
""",
    sql"""
create table service_settings (
  id bigint auto_increment primary key not null,
  maximum_accounts bigint not null default 10000,
  service_no bigint not null references services(no),
  created_at timestamp not null,
  updated_at timestamp not null
)
"""
  )

  runIfFailed(sql"select count(1) from services")
}
