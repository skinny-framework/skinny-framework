package skinny.orm.feature

import org.joda.time.DateTime
import org.scalatest.{ fixture, Matchers }
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._
import scalikejdbc._

class TimestampsFeatureSpec
    extends fixture.FunSpec
    with Matchers
    with Connection
    with DBSeeds
    with Formatter
    with AutoRollback {

  addSeedSQL(
    sql"""
create table with_id (
  id bigint auto_increment primary key not null,
  created_at timestamp not null,
  updated_at timestamp
)""",
    sql"""
create table no_id (
  a int not null,
  b int not null,
  created_at timestamp not null,
  updated_at timestamp,
  primary key (a, b)
)""",
    sql"""
create table my_with_id (
  id bigint auto_increment primary key not null,
  my_created_at timestamp not null,
  my_updated_at timestamp
)"""
  )

  run()

  case class WithId(id: Long, createdAt: DateTime, updatedAt: DateTime)
  object WithId extends SkinnyCRUDMapper[WithId] with TimestampsFeature[WithId] {
    override def defaultAlias                                         = createAlias("wid")
    override def extract(rs: WrappedResultSet, n: ResultName[WithId]) = autoConstruct(rs, n)
  }

  case class NoId(a: Int, b: Int, createdAt: DateTime, updatedAt: DateTime)
  object NoId extends SkinnyNoIdCRUDMapper[NoId] with NoIdTimestampsFeature[NoId] {
    override def defaultAlias                                       = createAlias("noid")
    override def extract(rs: WrappedResultSet, n: ResultName[NoId]) = autoConstruct(rs, n)
  }

  case class MyWithId(id: Long, myCreatedAt: DateTime, myUpdatedAt: DateTime)
  object MyWithId extends SkinnyCRUDMapper[MyWithId] with TimestampsFeature[MyWithId] {
    override def defaultAlias                                           = createAlias("wid")
    override def extract(rs: WrappedResultSet, n: ResultName[MyWithId]) = autoConstruct(rs, n)
    override def createdAtFieldName                                     = "myCreatedAt"
    override def updatedAtFieldName                                     = "myUpdatedAt"
  }

  val (t1, t2) = (new DateTime(2000, 1, 1, 0, 0, 0), new DateTime(2000, 1, 2, 0, 0, 0))

  describe("WithId") {
    it("assigns/updates timestamps") { implicit session =>
      info("It automatically assigns createdAt and updatedAt.")
      val id      = WithId.createWithAttributes()
      val loaded1 = WithId.findById(id).get
      loaded1.createdAt should be(loaded1.updatedAt)
      loaded1.createdAt shouldNot be(null)

      info("Only updatedAt should be changed by update.")
      WithId.updateById(id).withAttributes()
      val loaded2 = WithId.findById(id).get
      loaded2.createdAt should be(loaded1.createdAt)
      loaded2.updatedAt shouldNot be(loaded1.updatedAt)

      info("Specified value is used when a user wants.")
      WithId.updateById(id).withAttributes("createdAt" -> t1, "updatedAt" -> t2)
      val loaded3 = WithId.findById(id).get
      loaded3.createdAt should be(t1)
      loaded3.updatedAt should be(t2)
    }
  }

  describe("WithoutId") {
    it("assigns/updates timestamps") { implicit session =>
      info("It automatically assigns createdAt and updatedAt.")
      NoId.createWithAttributes("a" -> 1, "b" -> 2)
      val findCond = sqls.eq(NoId.column.a, 1).and.eq(NoId.column.b, 2)
      val loaded1  = NoId.findBy(findCond).get
      loaded1.createdAt should be(loaded1.updatedAt)
      loaded1.createdAt shouldNot be(null)

      info("Only updatedAt should be changed by update.")
      NoId.updateBy(findCond).withAttributes()
      val loaded2 = NoId.findBy(findCond).get
      loaded2.createdAt should be(loaded1.createdAt)
      loaded2.updatedAt shouldNot be(loaded1.updatedAt)

      info("Specified value is used when a user wants.")
      NoId.updateBy(findCond).withAttributes("createdAt" -> t1, "updatedAt" -> t2)
      val loaded3 = NoId.findBy(findCond).get
      loaded3.createdAt should be(t1)
      loaded3.updatedAt should be(t2)
    }
  }

  describe("MyWithId") {
    it("assigns timestamps for custom fields") { implicit session =>
      val id      = MyWithId.createWithAttributes()
      val loaded1 = MyWithId.findById(id).get
      loaded1.myCreatedAt should be(loaded1.myUpdatedAt)
      loaded1.myCreatedAt shouldNot be(null)
    }
  }
}
