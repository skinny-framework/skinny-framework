package blog

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.{ Tag => _, _ }
import skinny.Pagination

class BlogSpec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("blog").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val postId =
      Post.createWithAttributes("title" -> "Hello World!", "body" -> "This is the first entry...")
    val scalaTagId = Tag.createWithAttributes("name" -> "Scala")
    val rubyTagId  = Tag.createWithAttributes("name" -> "Ruby")
    val pt         = PostTag.column
    insert.into(PostTag).namedValues(pt.postId -> postId, pt.tagId -> scalaTagId).toSQL.update.apply()
    insert.into(PostTag).namedValues(pt.postId -> postId, pt.tagId -> rubyTagId).toSQL.update.apply()
  }

  describe("hasManyThrough without byDefault") {
    it("should work as expected") { implicit session =>
      val id   = Post.limit(1).apply().head.id
      val post = Post.joins(Post.tagsRef).findById(id)
      post.get.tags.size should equal(2)
    }

    it("should work when joining twice") { implicit session =>
      val id   = Post.limit(1).apply().head.id
      val post = Post.joins(Post.tagsRef, Post.tagsRef).findById(id)
      post.get.tags.size should equal(2)
    }

    it("should work with BigDecimal") { implicit session =>
      val post = Post.limit(1).apply().head
      Post.updateById(post.id).withAttributes("viewCount" -> 123)
      Post.findById(post.id).get.viewCount should equal(123)
    }
  }

  describe("pagination with one-to-many relationships") {
    it("should work as expected") { implicit session =>
      // clear fixture data
      sql"truncate table posts".execute.apply()
      sql"truncate table tags".execute.apply()
      sql"truncate table posts_tags".execute.apply()

      // prepare data
      val tagIds = (1 to 10).map { i =>
        Tag.createWithAttributes("name" -> s"tag$i")
      }
      val pt = PostTag.column
      (1 to 10).map { i =>
        val id = Post.createWithAttributes("title" -> s"entry $i", "body" -> "foo bar baz")
        tagIds.take(3).foreach { tagId =>
          withSQL {
            insert.into(PostTag).namedValues(pt.postId -> id, pt.tagId -> tagId)
          }.update.apply()
        }
      }
      (11 to 20).map { i =>
        val id = Post.createWithAttributes("title" -> s"entry $i", "body" -> "bulah bulah...")
        tagIds.take(4).foreach { tagId =>
          withSQL {
            insert.into(PostTag).namedValues(pt.postId -> id, pt.tagId -> tagId)
          }.update.apply()
        }
      }

      // #paginate in Querying
      {
        val posts = Post.joins(Post.tagsRef).paginate(Pagination.page(1).per(3)).apply()
        posts.size should equal(3)
        posts(0).tags.size should equal(3)
        posts(1).tags.size should equal(3)
        posts(2).tags.size should equal(3)
      }
      {
        val posts = Post.joins(Post.tagsRef).paginate(Pagination.page(7).per(3)).apply()
        posts.size should equal(2)
        posts(0).tags.size should equal(4)
        posts(1).tags.size should equal(4)
      }
      {
        val posts = Post.joins(Post.tagsRef).paginate(Pagination.page(8).per(3)).apply()
        posts.size should equal(0)
      }

      {
        val posts =
          Post.joins(Post.tagsRef).where("body" -> "foo bar baz").paginate(Pagination.page(1).per(3)).apply()
        posts.size should equal(3)
        posts(0).tags.size should equal(3)
        posts(1).tags.size should equal(3)
        posts(2).tags.size should equal(3)
      }
      {
        val posts =
          Post.joins(Post.tagsRef).where("body" -> "foo bar baz").paginate(Pagination.page(4).per(3)).apply()
        posts.size should equal(1)
        posts(0).tags.size should equal(3)
      }
      {
        val posts =
          Post.joins(Post.tagsRef).where("body" -> "foo bar baz").paginate(Pagination.page(5).per(3)).apply()
        posts.size should equal(0)
      }

      // #findAllWithPagination in Finder
      {
        val posts = Post.joins(Post.tagsRef).findAllWithPagination(Pagination.page(1).per(3))
        posts.size should equal(3)
        posts(0).tags.size should equal(3)
        posts(1).tags.size should equal(3)
        posts(2).tags.size should equal(3)
      }
      {
        val posts = Post.joins(Post.tagsRef).findAllWithPagination(Pagination.page(7).per(3))
        posts.size should equal(2)
        posts(0).tags.size should equal(4)
        posts(1).tags.size should equal(4)
      }
      {
        val posts = Post.joins(Post.tagsRef).findAllWithPagination(Pagination.page(8).per(3))
        posts.size should equal(0)
      }

      val p = Post.defaultAlias

      // #findAllByWithPagination in Finder
      {
        val posts =
          Post.joins(Post.tagsRef).findAllByWithPagination(sqls.eq(p.body, "foo bar baz"), Pagination.page(1).per(3))
        posts.size should equal(3)
        posts(0).tags.size should equal(3)
        posts(1).tags.size should equal(3)
        posts(2).tags.size should equal(3)
      }
      {
        val posts =
          Post.joins(Post.tagsRef).findAllByWithPagination(sqls.eq(p.body, "foo bar baz"), Pagination.page(4).per(3))
        posts.size should equal(1)
        posts(0).tags.size should equal(3)
      }
      {
        val posts =
          Post.joins(Post.tagsRef).findAllByWithPagination(sqls.eq(p.body, "foo bar baz"), Pagination.page(5).per(3))
        posts.size should equal(0)
      }

    }
  }
}
