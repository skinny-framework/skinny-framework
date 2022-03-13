package test001

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.{ fixture, Matchers }

/**
  * This spec demonstrates that using hasManyThrough.byDefault each other is impossible.
  */
class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("test001").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val t1_1 = Test1.createWithAttributes("name" -> "foo-1")
    val t1_2 = Test1.createWithAttributes("name" -> "foo-2")
    val t1_3 = Test1.createWithAttributes("name" -> "foo-3")
    val t2_1 = Test2.createWithAttributes("name" -> "bar-1")
    val t2_2 = Test2.createWithAttributes("name" -> "bar-2")

    Test1Test2.createWithAttributes("test1Id" -> t1_1, "test2Id" -> t2_1)
    Test1Test2.createWithAttributes("test1Id" -> t1_1, "test2Id" -> t2_2)
    Test1Test2.createWithAttributes("test1Id" -> t1_2, "test2Id" -> t2_2)
  }

  describe("hasManyThrough byDefault each other") {
    it("should work as expected") { implicit session =>
      val (t1, t2) = (Test1.defaultAlias, Test2.defaultAlias)

      val t1_1 = Test1.findBy(sqls.eq(t1.name, "foo-1"))
      t1_1.isDefined should equal(true)
      t1_1.get.test2.map(_.name).sorted should equal(Seq("bar-1", "bar-2"))

      val t1_2 = Test1.findBy(sqls.eq(t1.name, "foo-2"))
      t1_2.isDefined should equal(true)
      t1_2.get.test2.map(_.name).sorted should equal(Seq("bar-2"))

      val t1_3 = Test1.findBy(sqls.eq(t1.name, "foo-3"))
      t1_3.isDefined should equal(true)
      t1_3.get.test2 should equal(Nil)

      val t2_1 = Test2.joins(Test2.test1Ref).findBy(sqls.eq(t2.name, "bar-1"))
      t2_1.isDefined should equal(true)
      t2_1.get.test1.map(_.name).sorted should equal(Seq("foo-1"))

      val t2_2 = Test2.joins(Test2.test1Ref).findBy(sqls.eq(t2.name, "bar-2"))
      t2_2.isDefined should equal(true)
      t2_2.get.test1.map(_.name).sorted should equal(Seq("foo-1", "foo-2"))
    }
  }

}
