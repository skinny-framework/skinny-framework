package dev

import skinny.dbmigration._
import scalikejdbc._, SQLInterpolation._

object DBInitializer extends DBSeeds {

  addSeedSQL(
    sql"create sequence companies_id_seq start with 1",
    sql"""
      create table companies (
        id bigint not null default nextval('companies_id_seq') primary key,
        name varchar(64) not null,
        url varchar(128),
        created_at timestamp not null,
        updated_at timestamp
      )
    """
  )

  addSeed {
    DB localTx { implicit s =>
      sql"insert into companies (name, url, created_at) values (?, ?, current_timestamp)".batch(
        Seq("Google", "http://www.google.com/"),
        Seq("Microsoft", "http://www.microsoft.com/"),
        Seq("Yahoo!", "http://www.yahoo.com/")
      ).apply()
    }
  }

}

