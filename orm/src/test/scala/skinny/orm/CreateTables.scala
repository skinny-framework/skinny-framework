package skinny.orm

import scalikejdbc._
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
""",
    sql"""
create table books (
  isbn varchar(64) not null,
  title varchar(255) not null,
  description varchar(1024)
)
""",
    sql"""
create table isbn_master (
  isbn varchar(64) not null,
  publisher varchar(255) not null
)
""",
    sql"""
create table products (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  price_yen bigint not null
)
""",
    sql"""
create table tag (
  tag varchar(255) primary key not null
)
""",
    sql"""
create table tag_description (
  tag varchar(255) primary key not null,
  description varchar(1024) not null
)
""",
    sql"""
create table tag2 (
  tag varchar(255) primary key not null
)
""",
    sql"""
create table tag_description2 (
  tag varchar(255) primary key not null,
  description varchar(1024) not null
)
""",
    sql"""
create table legacy_accounts (
  account_code varchar(12) not null,
  user_id int,
  name varchar(255)
)
""",
    sql"""
create table table1 (
  num int not null,
  name varchar(128) not null
)
""",
    sql"""
create table table2 (
  label varchar(128) not null
)
"""

  )

  runIfFailed(sql"select * from members")
}
