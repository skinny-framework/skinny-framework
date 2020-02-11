package test009

import org.joda.time.DateTime
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.dbmigration.DBSeeds
import skinny.orm._

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test009", "jdbc:h2:mem:test009;MODE=PostgreSQL", "sa", "sa")
}

trait CreateTables extends DBSeeds { self: Connection =>
  override val dbSeedsAutoSession = NamedAutoSession("test009")
  addSeedSQL(sql"""
   create table blog (
     id bigserial not null,
     name varchar(100) not null,
     created_at timestamp not null default current_timestamp
   )""")
  addSeedSQL(sql"""
   create table tag (
     id bigserial not null,
     value varchar(100) not null,
     created_at timestamp not null default current_timestamp
   )""")
  addSeedSQL(sql"""
   create table blog_tag (
     id bigserial not null,
     blog_id bigint not null references blog(id),
     tag_id bigint not null references tag(id)
   )""")
  runIfFailed(sql"select count(1) from blog")
}

class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("test009").toDB()

  case class Blog(id: Long, name: String, createdAt: DateTime, tags: Seq[Tag] = Nil)
  case class BlogTag(blogId: Long, tagId: Long)
  case class Tag(id: Long, value: String, createdAt: DateTime)

  object Blog extends SkinnyCRUDMapper[Blog] {
    override val connectionPoolName                                  = "test009"
    override def defaultAlias                                        = createAlias("b")
    override def extract(rs: WrappedResultSet, rn: ResultName[Blog]) = autoConstruct(rs, rn, "tags")

    override val defaultOrderings: Seq[SQLSyntax] = Seq(
      sqls"${defaultAlias.name} DESC",
      sqls"${defaultAlias.createdAt} DESC"
    )

    hasManyThrough[BlogTag, Tag](
      through = BlogTag -> BlogTag.defaultAlias,
      throughOn = (tag, blogTag) => sqls.eq(tag.id, blogTag.tagId),
      many = Tag -> Tag.defaultAlias,
      on = (blogTag, tag) => sqls.eq(blogTag.tagId, tag.id),
      merge = (blog, tags) => blog.copy(tags = tags)
    ).byDefault
  }

  object BlogTag extends SkinnyCRUDMapper[BlogTag] {
    override val connectionPoolName                                     = "test009"
    override def defaultAlias                                           = createAlias("bt")
    override def extract(rs: WrappedResultSet, rn: ResultName[BlogTag]) = autoConstruct(rs, rn)
  }

  object Tag extends SkinnyCRUDMapper[Tag] {
    override val connectionPoolName                                 = "test009"
    override def defaultAlias                                       = createAlias("t")
    override def extract(rs: WrappedResultSet, rn: ResultName[Tag]) = autoConstruct(rs, rn)
  }

  def dataPreparation()(implicit s: DBSession) = {
    val blog1 = Blog.createWithAttributes("name" -> "Apply in America")
    val blog2 = Blog.createWithAttributes("name" -> "Apply in Brazil")
    val blog3 = Blog.createWithAttributes("name" -> "Apply in China")
    val blog4 = Blog.createWithAttributes("name" -> "Apply in Tokyo")
    val blog5 = Blog.createWithAttributes("name" -> "Apply in NY")

    val tag1 = Tag.createWithAttributes("value" -> "scala")
    val tag2 = Tag.createWithAttributes("value" -> "java")
    val tag3 = Tag.createWithAttributes("value" -> "ruby")

    BlogTag.createWithAttributes("blogId" -> blog4, "tagId" -> tag2)
    BlogTag.createWithAttributes("blogId" -> blog4, "tagId" -> tag1)
    BlogTag.createWithAttributes("blogId" -> blog5, "tagId" -> tag3)
  }

  describe("findAllWithLimitOffset") {
    it("should work") { implicit session =>
      dataPreparation()

      val blogs = Blog.findAllWithLimitOffset(limit = 2)
      blogs.map(_.name) should equal(Seq("Apply in Tokyo", "Apply in NY"))
    }
  }

  // NOTE: before fixing a bug, this test failed
  describe("querying") {
    it("should work") { implicit session =>
      dataPreparation()

      val blogs = Blog.limit(2).apply()
      blogs.map(_.name) should equal(Seq("Apply in Tokyo", "Apply in NY"))
    }
  }

}
