package test006

import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test006", "jdbc:h2:mem:test006;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>
  override val dbSeedsAutoSession = NamedAutoSession("test006")
  addSeedSQL(sql"create table summary (id bigserial not null, name varchar(100) not null)")
  runIfFailed(sql"select count(1) from summary")
}

class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {
  override def db(): DB = NamedDB("test006").toDB()

  var (_beforeCreate, _beforeUpdateBy, _beforeDeleteBy, _afterCreate, _afterDeleteBy, _afterUpdateBy) =
    (0, 0, 0, 0, 0, 0)

  case class Summary(id: Long, name: String)
  object Summary extends SkinnyCRUDMapper[Summary] {
    override val connectionPoolName = "test006"
    override def defaultAlias       = createAlias("s")

    beforeCreate((session: DBSession, namedValues: Seq[(SQLSyntax, Any)]) => {
      _beforeCreate += 1
    })
    afterCreate((session: DBSession, namedValues: Seq[(SQLSyntax, Any)], generatedId: Option[Long]) => {
      _afterCreate += 1
    })

    beforeUpdateBy((s: DBSession, where: SQLSyntax, params: Seq[(SQLSyntax, Any)]) => {
      _beforeUpdateBy += 1
    })
    afterUpdateBy((s: DBSession, where: SQLSyntax, params: Seq[(SQLSyntax, Any)], count: Int) => {
      _afterUpdateBy += 1
    })

    beforeDeleteBy((s: DBSession, where: SQLSyntax) => {
      _beforeDeleteBy += 1
    })
    afterDeleteBy((s: DBSession, where: SQLSyntax, deletedCount: Int) => {
      _afterDeleteBy += 1
    })

    beforeCreate((session: DBSession, namedValues: Seq[(SQLSyntax, Any)]) => {
      _beforeCreate += 1
    })
    afterCreate((session: DBSession, namedValues: Seq[(SQLSyntax, Any)], generatedId: Option[Long]) => {
      _afterCreate += 1
    })

    beforeUpdateBy((s: DBSession, where: SQLSyntax, params: Seq[(SQLSyntax, Any)]) => {
      _beforeUpdateBy += 1
    })
    afterUpdateBy((s: DBSession, where: SQLSyntax, params: Seq[(SQLSyntax, Any)], count: Int) => {
      _afterUpdateBy += 1
    })

    beforeDeleteBy((s: DBSession, where: SQLSyntax) => {
      _beforeDeleteBy += 1
    })
    afterDeleteBy((s: DBSession, where: SQLSyntax, deletedCount: Int) => {
      _afterDeleteBy += 1
    })

    override def extract(rs: WrappedResultSet, rn: ResultName[Summary]) = autoConstruct(rs, rn)
  }

  describe("before/after") {
    it("should work") { implicit session =>
      _beforeCreate should equal(0)
      _afterCreate should equal(0)
      _beforeUpdateBy should equal(0)
      _afterUpdateBy should equal(0)
      _beforeDeleteBy should equal(0)
      _afterDeleteBy should equal(0)

      val id = Summary.createWithAttributes("name" -> "Sample")
      Summary.updateById(id).withAttributes("name" -> "Sample2")
      Summary.deleteById(id)

      _beforeCreate should equal(2)
      _afterCreate should equal(2)
      _beforeUpdateBy should equal(2)
      _afterUpdateBy should equal(2)
      _beforeDeleteBy should equal(2)
      _afterDeleteBy should equal(2)
    }
  }
}
