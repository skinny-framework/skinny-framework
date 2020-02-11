package test002

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.{ fixture, Matchers }

class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("test002").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    Account.createWithAttributes("name" -> "Alice")
    Account.createWithAttributes("name" -> "Bob")
  }

  describe("SkinnyNoIdMapper#findAll") {
    it("should work as expected") { implicit session =>
      val a = Account.defaultAlias

      {
        val accounts = Account.findAll()
        accounts.size should equal(2)
      }
      {
        val accounts = Account.findAll(Seq(a.name))
        accounts.size should equal(2)
        accounts.map(_.name) should equal(Seq("Alice", "Bob"))
      }
      {
        val accounts = Account.findAll(Seq(a.name.desc))
        accounts.size should equal(2)
        accounts.map(_.name) should equal(Seq("Bob", "Alice"))
      }
    }
  }

  /**
    * [info] - should work as expected *** FAILED ***
    * [info]   org.h2.jdbc.JdbcSQLException: Syntax error in SQL statement "SELECT A.NAME AS N_ON_A FROM ACCOUNT A  WHERE  A.NAME = ?  ORDER BY  [*]"; expected "=, NOT, EXISTS, INTERSECTS"; SQL statement:
    * [info] select a.name as n_on_a from account a  where  a.name = ?  order by  [42001-179]
    * [info]   at org.h2.message.DbException.getJdbcSQLException(DbException.java:345)
    */
  describe("SkinnyNoIdMapper#findAllBy") {
    it("should work as expected") { implicit session =>
      val a = Account.defaultAlias

      {
        val accounts = Account.findAllBy(sqls.eq(a.name, "Bob"))
        accounts.size should equal(1)
      }
      {
        val accounts = Account.findAllBy(sqls.eq(a.name, "Bob"), Seq(a.name))
        accounts.size should equal(1)
        accounts.map(_.name) should equal(Seq("Bob"))
      }
      {
        val accounts = Account.findAllBy(sqls.eq(a.name, "Bob"), Seq(a.name.desc))
        accounts.size should equal(1)
        accounts.map(_.name) should equal(Seq("Bob"))
      }
    }
  }

}
