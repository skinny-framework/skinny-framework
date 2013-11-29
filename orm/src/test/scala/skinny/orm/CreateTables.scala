package skinny.orm

import scalikejdbc._, SQLInterpolation._
import skinny.dbmigration.DBSeeds

trait CreateTables extends DBSeeds { self: Connection =>

  addSeedSQL(
    sql"""
create table members (
  id bigint auto_increment primary key not null,
  country_id bigint not null,
  company_id bigint,
  mentor_id bigint,
  created_at timestamp not null
)
""",
    sql"""
create table members2 (
  id bigint auto_increment primary key not null,
  country_id bigint not null,
  company_id bigint,
  mentor_id bigint,
  created_at timestamp not null
)
""",
    sql"""
create table names (
  member_id bigint primary key not null,
  first varchar(64) not null,
  last varchar(64) not null,
  created_at timestamp not null,
  updated_at timestamp
)
""",
    sql"""
create table countries (
  id bigint auto_increment primary key not null,
  name varchar(255) not null
)
""",
    sql"""
create table companies (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  country_id bigint,
  is_deleted boolean default false not null
)
""",
    sql"""
create table groups (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  deleted_at timestamp
)
""",
    sql"""
create table groups_members (
  group_id bigint not null,
  member_id bigint not null
)
""",
    sql"""
create table skills (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  created_at timestamp not null,
  updated_at timestamp,
  -- lock_version bigint not null default 1
  lock_version bigint not null
)
""",
    sql"""
create table members_skills (
  member_id bigint not null,
  skill_id bigint not null
)
"""
  )

  runIfFailed(sql"select * from members")
}
