package whitespace

import scalikejdbc._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("ws")

  addSeedSQL(
    sql"""
create table posts (
  id bigint auto_increment primary key not null,
  title varchar(128) not null,
  body varchar(1024) not null,
  view_count number(3) not null default 0,
  created_at timestamp not null,
  updated_at timestamp
)
""",
    sql"""
create table tags (
  id bigint auto_increment primary key not null,
  name varchar(128) not null,
  created_at timestamp not null,
  updated_at timestamp
)
""",
    sql"""
create table posts_tags (
  post_id bigint not null,
  tag_id bigint not null
)
"""
  )

  runIfFailed(sql"select count(1) from posts")
}
