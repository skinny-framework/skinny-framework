package test005

import org.scalatest.funspec.FixtureAnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test005", "jdbc:h2:mem:test005;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("test005")

  addSeedSQL(sql"create table summary (id bigserial not null, name varchar(100) not null)")
  addSeedSQL(sql"create table data1 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data2 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data3 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data4 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data5 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data6 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data7 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data8 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data9 (id bigserial not null, summary_id bigint not null references summary(id))")
  addSeedSQL(sql"create table data10 (id bigserial not null, summary_id bigint not null references summary(id))")
  runIfFailed(sql"select count(1) from summary")
}

class Spec extends FixtureAnyFunSpec with Matchers with Connection with CreateTables with AutoRollback {

  case class Summary(
      id: Long,
      name: String,
      data1: Seq[Data1] = Nil,
      data2: Seq[Data2] = Nil,
      data3: Seq[Data3] = Nil,
      data4: Seq[Data4] = Nil,
      data5: Seq[Data5] = Nil,
      data6: Seq[Data6] = Nil,
      data7: Seq[Data7] = Nil,
      data8: Seq[Data8] = Nil,
      data9: Seq[Data9] = Nil,
      data10: Seq[Data10] = Nil
  )

  case class Data1(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data2(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data3(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data4(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data5(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data6(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data7(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data8(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data9(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)
  case class Data10(id: Long, summaryId: Option[Long], summary: Option[Summary] = None)

  object Summary extends SkinnyCRUDMapper[Summary] {
    override val connectionPoolName = "test005"
    override def defaultAlias       = createAlias("s")
    override def extract(rs: WrappedResultSet, rn: ResultName[Summary]) = {
      autoConstruct(rs, rn, "data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9", "data10")
    }
    lazy val d1 = hasMany[Data1](
      Data1 -> Data1.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data1 = d)
    )
    lazy val d2 = hasMany[Data2](
      Data2 -> Data2.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data2 = d)
    )
    lazy val d3 = hasMany[Data3](
      Data3 -> Data3.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data3 = d)
    )
    lazy val d4 = hasMany[Data4](
      Data4 -> Data4.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data4 = d)
    )
    lazy val d5 = hasMany[Data5](
      Data5 -> Data5.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data5 = d)
    )
    lazy val d6 = hasMany[Data6](
      Data6 -> Data6.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data6 = d)
    )
    lazy val d7 = hasMany[Data7](
      Data7 -> Data7.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data7 = d)
    )
    lazy val d8 = hasMany[Data8](
      Data8 -> Data8.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data8 = d)
    )
    lazy val d9 = hasMany[Data9](
      Data9 -> Data9.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data9 = d)
    )
    lazy val d10 = hasMany[Data10](
      Data10 -> Data10.defaultAlias,
      (s, d) => sqls.eq(s.id, d.summaryId),
      (s, d) => s.copy(data10 = d)
    )

    def withAssociations = joins(d1, d2, d3, d4, d5, d6, d7, d8, d9)
  }
  object Data1 extends SkinnyCRUDMapper[Data1] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d1")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data1]) = autoConstruct(rs, rn, "summary")
  }
  object Data2 extends SkinnyCRUDMapper[Data2] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d2")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data2]) = autoConstruct(rs, rn, "summary")
  }
  object Data3 extends SkinnyCRUDMapper[Data3] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d3")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data3]) = autoConstruct(rs, rn, "summary")
  }
  object Data4 extends SkinnyCRUDMapper[Data4] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d4")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data4]) = autoConstruct(rs, rn, "summary")
  }
  object Data5 extends SkinnyCRUDMapper[Data5] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d5")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data5]) = autoConstruct(rs, rn, "summary")
  }
  object Data6 extends SkinnyCRUDMapper[Data6] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d6")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data6]) = autoConstruct(rs, rn, "summary")
  }
  object Data7 extends SkinnyCRUDMapper[Data7] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d7")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data7]) = autoConstruct(rs, rn, "summary")
  }
  object Data8 extends SkinnyCRUDMapper[Data8] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d8")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data8]) = autoConstruct(rs, rn, "summary")
  }
  object Data9 extends SkinnyCRUDMapper[Data9] {
    override val connectionPoolName                                   = "test005"
    override def defaultAlias                                         = createAlias("d9")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data9]) = autoConstruct(rs, rn, "summary")
  }
  object Data10 extends SkinnyCRUDMapper[Data10] {
    override val connectionPoolName                                    = "test005"
    override def defaultAlias                                          = createAlias("d10")
    override def extract(rs: WrappedResultSet, rn: ResultName[Data10]) = autoConstruct(rs, rn, "summary")
  }

  override def db(): DB = NamedDB("test005").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val summaryId = Summary.createWithAttributes("name" -> "Sample")
    Data1.createWithAttributes("summaryId" -> summaryId)
    Data1.createWithAttributes("summaryId" -> summaryId)
    Data1.createWithAttributes("summaryId" -> summaryId)

    Data2.createWithAttributes("summaryId" -> summaryId)

    Data3.createWithAttributes("summaryId" -> summaryId)
    Data3.createWithAttributes("summaryId" -> summaryId)

    Data4.createWithAttributes("summaryId" -> summaryId)

    Data5.createWithAttributes("summaryId" -> summaryId)
    Data5.createWithAttributes("summaryId" -> summaryId)
    Data5.createWithAttributes("summaryId" -> summaryId)
    Data5.createWithAttributes("summaryId" -> summaryId)
    Data5.createWithAttributes("summaryId" -> summaryId)

    Data6.createWithAttributes("summaryId" -> summaryId)

    Data7.createWithAttributes("summaryId" -> summaryId)

    Data8.createWithAttributes("summaryId" -> summaryId)

    Data9.createWithAttributes("summaryId" -> summaryId)
    Data9.createWithAttributes("summaryId" -> summaryId)
  }

  describe("Entity which has 1 - 8 associations") {
    it("should return results as expected") { implicit session =>
      import Summary._
      Summary.joins(d1).count() should equal(1)
      Summary.joins(d1, d2).count() should equal(1)
      Summary.joins(d1, d2, d3).count() should equal(1)
      Summary.joins(d1, d2, d3, d4).count() should equal(1)
      Summary.joins(d1, d2, d3, d4, d5).count() should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6).count() should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6, d7).count() should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6, d7, d8).count() should equal(1)

      Summary.joins(d1).findAll().size should equal(1)
      Summary.joins(d1, d2).findAll().size should equal(1)
      Summary.joins(d1, d2, d3).findAll().size should equal(1)
      Summary.joins(d1, d2, d3, d4).findAll().size should equal(1)
      Summary.joins(d1, d2, d3, d4, d5).findAll().size should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6).findAll().size should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6, d7).findAll().size should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6, d7, d8).findAll().size should equal(1)
    }
  }
  describe("Entity which has 9 associations") {
    it("should return results as expected") { implicit session =>
      Summary.count() should equal(1)
      Summary.withAssociations.count() should equal(1)
      val s = Summary.withAssociations.findAll().head
      s.data1.size should equal(3)
      s.data2.size should equal(1)
      s.data3.size should equal(2)
      s.data4.size should equal(1)
      s.data5.size should equal(5)
      s.data6.size should equal(1)
      s.data7.size should equal(1)
      s.data8.size should equal(1)
      s.data9.size should equal(2)
    }
  }

  describe("Entity which has 10 associations") {
    it("should throw exception as expected") { implicit session =>
      import Summary._
      Summary.joins(d1, d2, d3, d4, d5, d6, d7, d8, d9, d9).findAll().size should equal(1)
      Summary.joins(d1, d2, d3, d4, d5, d6, d7, d8, d9, d10).count()
      intercept[IllegalStateException] {
        Summary.joins(d1, d2, d3, d4, d5, d6, d7, d8, d9, d10).findAll()
      }
    }
  }

}
