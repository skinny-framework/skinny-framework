package lib

import scalikejdbc._
import skinny.dbmigration.DBSeeds

object DBInitializer extends DBSeeds {

  addSeed {
    DB localTx { implicit s =>
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

      val insertProgrammer = sql"insert into programmer (name, favorite_number, company_id, created_timestamp) values (?, ?, ?, current_timestamp)"
      val programmer1 = insertProgrammer.bind("Alice", 123456, company1).updateAndReturnGeneratedKey.apply()
      val programmer2 = insertProgrammer.bind("Bob", 777, company2).updateAndReturnGeneratedKey.apply()

      insertProgrammer.batch(
        Seq("Chris", 24, company1),
        Seq("Denis", 10, company2),
        Seq("Eric", 99999999, company2)
      ).apply()

      sql"insert into programmer_skill (programmer_id, skill_id) values (?, ?)".batch(
        Seq(programmer1, skill1),
        Seq(programmer2, skill1),
        Seq(programmer2, skill2)
      ).apply()
    }
  }

  def initialize() = runUnless {
    DB readOnly { implicit s =>
      sql"select count(1) from company".map(_.long(1)).single.apply().get > 0
    }
  }

}
