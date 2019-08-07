package issue229

import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add(Symbol("issue229"), "jdbc:h2:mem:issue229;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession(Symbol("issue229"))

  addSeedSQL(
    sql"""
create table user (
  id bigserial not null,
  name varchar(100) not null)
"""
  )
  addSeedSQL(
    sql"""
create table article (
  id bigserial not null,
  title varchar(100) not null,
  user_id bigint references user(id))
"""
  )
  runIfFailed(sql"select count(1) from article")
}

class Issue229Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  case class User(id: Long, name: String)
  case class Article(id: Long, title: String, userId: Option[Long], user: Option[User] = None)

  object User extends SkinnyCRUDMapper[User] {
    override val connectionPoolName = Symbol("issue229")
    override def defaultAlias       = createAlias("u")

    override def extract(rs: WrappedResultSet, rn: ResultName[User]) = autoConstruct(rs, rn)
  }
  object Article extends SkinnyCRUDMapper[Article] {
    override val connectionPoolName                                     = Symbol("issue229")
    override def defaultAlias                                           = createAlias("a")
    override def extract(rs: WrappedResultSet, rn: ResultName[Article]) = autoConstruct(rs, rn, "user")

    lazy val userRef = {
      belongsTo[User](
        right = User,
        merge = (a, u) => a.copy(user = u)
      ).includes[User](
        (as, us) =>
          as.map { a =>
            us.find(u => a.user.exists(_.id == u.id))
              .map(u => a.copy(user = Some(u)))
              .getOrElse(a)
        }
      )
    }
  }

  override def db(): DB = NamedDB(Symbol("issue229")).toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val aliceId = User.createWithAttributes(Symbol("name") -> "Alice")
    val bobId   = User.createWithAttributes(Symbol("name") -> "Bob")
    Seq(
      ("Hello World", Some(aliceId)),
      ("Getting Started with Scala", Some(bobId)),
      ("Functional Programming", None),
      ("How to user sbt", Some(aliceId))
    ).foreach {
      case (title, userId) => Article.createWithAttributes(Symbol("title") -> title, Symbol("userId") -> userId)
    }
  }

  def id(implicit session: DBSession): Long = {
    Article.where(Symbol("title") -> "Functional Programming").apply().head.id
  }

  describe("find empty with empty ids") {
    it("should return no results") { implicit session =>
      Article.where(Symbol("id") -> Nil).apply().size should equal(0)
    }
  }

}
