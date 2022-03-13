package issue347

import org.joda.time.DateTime
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("issue347", "jdbc:h2:mem:issue347;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("issue347")

  addSeedSQL(
    sql"""
create table user (
  user_id bigserial not null,
  name varchar(100) not null,
  created_at timestamp not null default current_timestamp)
"""
  )
  addSeedSQL(
    sql"""
create table article (
  id bigserial not null,
  title varchar(100) not null,
  user_id bigint references user(user_id))
"""
  )
  runIfFailed(sql"select count(1) from article")
}

class Issue347Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  case class User(userId: Long, name: String, createdAt: DateTime, articles: Seq[Article] = Nil)
  case class Article(id: Long, title: String, userId: Option[Long], user: Option[User] = None)

  object User extends SkinnyCRUDMapper[User] {
    override val connectionPoolName  = "issue347"
    override val primaryKeyFieldName = "userId"
    override def defaultAlias        = createAlias("u")

    lazy val articlesRef = hasMany[Article](
      many = Article -> Article.defaultAlias,
      on = (u, a) => sqls.eq(u.userId, a.userId),
      merge = (u, as) => u.copy(articles = as)
    ).includes[Article](
      merge = { (users, articles) =>
        users.map { user =>
          user.copy(articles = articles.filter(_.userId.exists(_ == user.userId)))
        }
      }
    )

    override def extract(rs: WrappedResultSet, rn: ResultName[User]) = autoConstruct(rs, rn, "articles")
  }

  object Article extends SkinnyCRUDMapper[Article] {
    override val connectionPoolName                                     = "issue347"
    override def defaultAlias                                           = createAlias("a")
    override def extract(rs: WrappedResultSet, rn: ResultName[Article]) = autoConstruct(rs, rn, "user")

    lazy val userRef = belongsTo[User](
      right = User,
      merge = (a, u) => a.copy(user = u)
    )
  }

  override def db(): DB = NamedDB("issue347").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val aliceId = User.createWithAttributes("name" -> "Alice")
    val bobId   = User.createWithAttributes("name" -> "Bob") // Scala
    val chrisId = User.createWithAttributes("name" -> "Chris") // Scala
    val denId   = User.createWithAttributes("name" -> "Den")
    val ericId  = User.createWithAttributes("name" -> "Eric") // Scala
    val fredId  = User.createWithAttributes("name" -> "Fred")

    val titleAndUser = Seq(
      ("Hello World", Some(aliceId)),
      ("Getting Started with Scala", Some(bobId)), // Scala
      ("Functional Programming in Scala", None), // Scala
      ("Beginning Ruby", Some(aliceId)),
      ("Beginning Scala", Some(chrisId)), // Scala
      ("Beginning Ruby", Some(denId)),
      ("Hello Scala", Some(ericId)), // Scala
      ("Bob's Scala Lesson 1", Some(bobId)), // Scala
      ("Functional Programming in Java", Some(fredId)),
      ("Beginning Ruby", Some(fredId)),
      ("Scalaz Usage", Some(chrisId)), // Scala
      ("The Better Java?", Some(bobId)),
      ("How to user sbt", Some(aliceId))
    )
    titleAndUser.foreach {
      case (title, userId) =>
        Article.createWithAttributes("title" -> title, "userId" -> userId)
    }
  }

  describe("joins/includes") {

    it("should return expected results when joins / includes") { implicit session =>
      val users1 = User.joins(User.articlesRef).findAll()
      val users2 = User.includes(User.articlesRef).findAll()
      println(users1)
      println(users2)
      users1 should equal(users2)
    }
  }

}
