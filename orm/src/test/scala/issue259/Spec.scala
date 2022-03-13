package issue259

import org.joda.time.DateTime
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("issue259", "jdbc:h2:mem:issue259;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>

  override val dbSeedsAutoSession = NamedAutoSession("issue259")

  addSeedSQL(
    sql"""
create table user (
  id bigserial not null,
  name varchar(100) not null,
  created_at timestamp not null default current_timestamp)
"""
  )
  addSeedSQL(
    sql"""
create table nickname (
  name varchar(100) not null,
  user_id bigint not null references user(id),
  created_at timestamp not null default current_timestamp)
"""
  )
  addSeedSQL(
    sql"""
create table article (
  title varchar(100) not null,
  user_id bigint references user(id))
"""
  )
  runIfFailed(sql"select count(1) from article")
}

class HasOneHasManySpec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  case class User(
      id: Long,
      name: String,
      createdAt: DateTime,
      articles: Seq[Article] = Nil,
      nickname: Option[Nickname] = None
  )

  case class Nickname(name: String, userId: Long, user: Option[User] = None)
  case class Article(title: String, userId: Option[Long], user: Option[User] = None)

  object User extends SkinnyCRUDMapper[User] {
    override val connectionPoolName = "issue259"
    override def defaultAlias       = createAlias("u")

    lazy val articlesRef = hasMany[Article](
      many = Article -> Article.defaultAlias,
      on = (u, a) => sqls.eq(u.id, a.userId),
      merge = (u, as) => u.copy(articles = as)
    )
    lazy val nicknameRef = hasOne[Nickname](
      right = Nickname,
      merge = (u, n) => u.copy(nickname = n)
    )

    override def extract(rs: WrappedResultSet, rn: ResultName[User]) = {
      autoConstruct(rs, rn, "articles", "nickname")
    }
  }
  object Nickname extends SkinnyNoIdCRUDMapper[Nickname] {
    override val connectionPoolName                                      = "issue259"
    override def defaultAlias                                            = createAlias("n")
    override def extract(rs: WrappedResultSet, rn: ResultName[Nickname]) = autoConstruct(rs, rn, "user")

    lazy val userRef = belongsTo[User](
      right = User,
      merge = (n, u) => n.copy(user = u)
    )
  }
  object Article extends SkinnyNoIdCRUDMapper[Article] {
    override val connectionPoolName                                     = "issue259"
    override def defaultAlias                                           = createAlias("a")
    override def extract(rs: WrappedResultSet, rn: ResultName[Article]) = autoConstruct(rs, rn, "user")

    lazy val userRef = belongsTo[User](
      right = User,
      merge = (a, u) => a.copy(user = u)
    )
  }

  override def db(): DB = NamedDB("issue259").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val aliceId = User.createWithAttributes("name" -> "Alice")
    val bobId   = User.createWithAttributes("name" -> "Bob") // Scala
    val chrisId = User.createWithAttributes("name" -> "Chris") // Scala
    val denId   = User.createWithAttributes("name" -> "Den")
    val ericId  = User.createWithAttributes("name" -> "Eric") // Scala
    val fredId  = User.createWithAttributes("name" -> "Fred")

    Nickname.createWithAttributes("userId" -> bobId, "name" -> "Bobby")

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

  describe("Finder pagination with hasMany conditions") {
    lazy val a = Article.defaultAlias
    lazy val u = User.defaultAlias

    it("should return has-many associations") { implicit session =>
      val matchesScala = sqls.like(a.title, LikeConditionEscapeUtil.contains("Scala"))
      val users = User
        .joins(User.articlesRef)
        .findAllByWithLimitOffset(
          where = matchesScala,
          limit = 2,
          offset = 1,
          orderings = Seq(u.name.desc, a.title)
        )

      users.map(_.name) should equal(Seq("Chris", "Bob"))
      users.forall(_.nickname.isEmpty) should be(true)
      // ascending order, shouldn't include "The Better Java?"
      users.find(_.name == "Bob").map(_.articles.map(_.title)) should equal(
        Some(Seq("Bob's Scala Lesson 1", "Getting Started with Scala"))
      )
    }

    it("should return has-one associations") { implicit session =>
      val users = User
        .joins(User.nicknameRef)
        .findAllByWithLimitOffset(
          where = sqls.in(u.name, Seq("Bob", "Chris")),
          limit = 2,
          orderings = Seq(u.name.desc)
        )

      users.map(_.name) should equal(Seq("Chris", "Bob"))
      users.find(_.name == "Bob").exists(_.nickname.isDefined) should be(true)
      users.find(_.name == "Chris").exists(_.nickname.isDefined) should be(false)
    }

    it("should return has-one/has-many associations") { implicit session =>
      val matchesScala = sqls.like(a.title, LikeConditionEscapeUtil.contains("Scala"))
      val users = User
        .joins(User.articlesRef, User.nicknameRef)
        .findAllByWithLimitOffset(
          where = matchesScala,
          limit = 2,
          offset = 1,
          orderings = Seq(u.name.desc, a.title)
        )

      users.map(_.name) should equal(Seq("Chris", "Bob"))
      users.find(_.name == "Bob").exists(_.nickname.isDefined) should be(true)
      users.find(_.name == "Chris").exists(_.nickname.isDefined) should be(false)
      // ascending order, shouldn't include "The Better Java?"
      users.find(_.name == "Bob").map(_.articles.map(_.title)) should equal(
        Some(Seq("Bob's Scala Lesson 1", "Getting Started with Scala"))
      )
    }
  }

  describe("Querying pagination with hasMany conditions") {
    lazy val a = Article.defaultAlias
    lazy val u = User.defaultAlias

    it("should return has-many associations") { implicit session =>
      val matchesScala = sqls.like(a.title, LikeConditionEscapeUtil.contains("Scala"))
      val users = User
        .joins(User.articlesRef)
        .where(matchesScala)
        .limit(2)
        .offset(1)
        .orderBy(u.name.desc, a.title)
        .apply()

      users.map(_.name) should equal(Seq("Chris", "Bob"))
      users.forall(_.nickname.isEmpty) should be(true)
      // ascending order, shouldn't include "The Better Java?"
      users.find(_.name == "Bob").map(_.articles.map(_.title)) should equal(
        Some(Seq("Bob's Scala Lesson 1", "Getting Started with Scala"))
      )
    }

    it("should return has-one associations") { implicit session =>
      val users = User
        .joins(User.nicknameRef)
        .where(sqls.in(u.name, Seq("Bob", "Chris")))
        .limit(2)
        .orderBy(u.name.desc)
        .apply()

      users.map(_.name) should equal(Seq("Chris", "Bob"))
      users.find(_.name == "Bob").exists(_.nickname.isDefined) should be(true)
      users.find(_.name == "Chris").exists(_.nickname.isDefined) should be(false)
    }

    it("should return has-one/has-many associations") { implicit session =>
      val matchesScala = sqls.like(a.title, LikeConditionEscapeUtil.contains("Scala"))
      val users = User
        .joins(User.articlesRef, User.nicknameRef)
        .where(matchesScala)
        .limit(2)
        .offset(1)
        .orderBy(u.name.desc, a.title)
        .apply()

      users.map(_.name) should equal(Seq("Chris", "Bob"))
      users.find(_.name == "Bob").exists(_.nickname.isDefined) should be(true)
      users.find(_.name == "Chris").exists(_.nickname.isDefined) should be(false)
      // ascending order, shouldn't include "The Better Java?"
      users.find(_.name == "Bob").map(_.articles.map(_.title)) should equal(
        Some(Seq("Bob's Scala Lesson 1", "Getting Started with Scala"))
      )
    }
  }

}
