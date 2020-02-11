package test008

import org.joda.time.DateTime
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test008", "jdbc:h2:mem:test008;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>
  override val dbSeedsAutoSession = NamedAutoSession("test008")
  addSeedSQL(sql"create table blog (name varchar(100) not null)")
  addSeedSQL(sql"""
   create table article (
     blog_name varchar(100) not null references blog(name),
     title varchar(1000) not null,
     body text not null,
     created_at timestamp not null default current_timestamp
   )""")
  runIfFailed(sql"select count(1) from blog")
}

class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("test008").toDB()

  case class Blog(name: String)
  object Blog extends SkinnyNoIdCRUDMapper[Blog] {
    override val connectionPoolName                                  = "test008"
    override def defaultAlias                                        = createAlias("b")
    override def extract(rs: WrappedResultSet, rn: ResultName[Blog]) = autoConstruct(rs, rn)
  }

  case class Article(blogName: String, title: String, body: String, createdAt: DateTime, blog: Option[Blog] = None)
  object Article extends SkinnyNoIdCRUDMapper[Article] {
    override val connectionPoolName                                     = "test008"
    override def defaultAlias                                           = createAlias("a")
    override def extract(rs: WrappedResultSet, rn: ResultName[Article]) = autoConstruct(rs, rn, "blog")

    lazy val blogRef = belongsToWithAliasAndFkAndJoinCondition[Blog](
      right = Blog -> Blog.defaultAlias,
      fk = "blogName",
      on = sqls.eq(defaultAlias.blogName, Blog.defaultAlias.name),
      merge = (a, b) => a.copy(blog = b)
    )
  }

  describe("associations by default") {
    it("should work") { implicit session =>
      Blog.createWithAttributes("name" -> "Apply in Tokyo")
      Blog.createWithAttributes("name" -> "Apply in NY")
      Blog.createWithAttributes("name" -> "Apply in Paris")
      (1 to 5).foreach { day =>
        Article.createWithAttributes("title"    -> s"Learning Scala: Day $day",
                                     "body"     -> "日本へようこそ。東京は楽しいよ。",
                                     "blogName" -> "Apply in Tokyo")
      }
      (1 to 6).foreach { day =>
        Article.createWithAttributes("title"    -> s"Learning Scala: Day $day",
                                     "body"     -> "Welcome to New York!",
                                     "blogName" -> "Apply in NY")
      }
      (1 to 7).foreach { day =>
        Article.createWithAttributes("title"    -> s"Learning Scala: Day $day",
                                     "body"     -> "Bonjour et bienvenue à Paris!",
                                     "blogName" -> "Apply in Paris")
      }
      Article.joins(Article.blogRef).where("blogName" -> "Apply in Tokyo").apply().size should equal(5)
      Article.joins(Article.blogRef).where("blogName" -> "Apply in NY").apply().size should equal(6)
      Article.joins(Article.blogRef).where("blogName" -> "Apply in Paris").apply().size should equal(7)
    }
  }
}
