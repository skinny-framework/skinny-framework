package helper

import scalikejdbc._, SQLInterpolation._

object DBInitializer {

  def initialize() {
    DB readOnly { implicit s =>
      try {
        sql"select 1 from programmer limit 1".map(_.long(1)).single.apply()
      } catch {
        case e: java.sql.SQLException =>
          DB autoCommit { implicit s =>
            sql"""
create sequence programmer_id_seq start with 1;
create table programmer (
  id bigint not null default nextval('programmer_id_seq') primary key,
  name varchar(64) not null,
  company_id bigint,
  created_timestamp timestamp not null,
  updated_timestamp timestamp,
  deleted_timestamp timestamp
);

create sequence company_id_seq start with 1;
create table company (
  id bigint not null default nextval('company_id_seq') primary key,
  name varchar(64) not null,
  url varchar(128),
  created_at timestamp not null,
  updated_at timestamp,
  deleted_at timestamp
);

create sequence skill_id_seq start with 1;
create table skill (
  id bigint not null default nextval('skill_id_seq') primary key,
  name varchar(64) not null
);

create table programmer_skill (
  programmer_id bigint not null,
  skill_id bigint not null,
  primary key(programmer_id, skill_id)
);

alter table programmer add foreign key(company_id) references company(id);
alter table programmer_skill add foreign key(skill_id) references skill(id);
alter table programmer_skill add foreign key(programmer_id) references programmer(id);
   """.execute.apply()

            val company1 = sql"insert into company (name, url, created_at) values (?, ?, current_timestamp)"
              .bind("Typesafe", "http://typesafe.com/").updateAndReturnGeneratedKey.apply()
            val company2 = sql"insert into company (name, url, created_at) values (?, ?, current_timestamp)"
              .bind("Oracle", "http://twww.oracle.com/").updateAndReturnGeneratedKey.apply()

            sql"insert into company (name, url, created_at) values (?, ?, current_timestamp)".batch(
              Seq("Google", "http://www.google.com/"),
              Seq("Microsoft", "http://www.microsoft.com/")
            ).apply()

            val skill1 = sql"insert into skill (name) values (?)".bind("Scala").updateAndReturnGeneratedKey.apply()
            val skill2 = sql"insert into skill (name) values (?)".bind("Java").updateAndReturnGeneratedKey.apply()

            sql"insert into skill (name) values (?)".batch(
              Seq("Ruby"),
              Seq("MySQL"),
              Seq("PostgreSQL")
            ).apply()

            val programmer1 = sql"insert into programmer (name, company_id, created_timestamp) values (?, ?, current_timestamp)"
              .bind("Alice", company1).updateAndReturnGeneratedKey.apply()
            val programmer2 = sql"insert into programmer (name, company_id, created_timestamp) values (?, ?, current_timestamp)"
              .bind("Bob", company2).updateAndReturnGeneratedKey.apply()

            sql"insert into programmer (name, company_id, created_timestamp) values (?, ?, current_timestamp)".batch(
              Seq("Chris", company1),
              Seq("Denis", company2),
              Seq("Eric", company2)
            ).apply()

            sql"insert into programmer_skill (programmer_id, skill_id) values (?, ?)".batch(
              Seq(programmer1, skill1),
              Seq(programmer2, skill1),
              Seq(programmer2, skill2)
            ).apply()
          }
      }
    }
  }

}
