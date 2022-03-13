package whitespace

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.{ fixture, Matchers }
import skinny.test.FactoryGirl
import skinny.logging.Logging

class BlogSpec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback with Logging {

  override def db(): DB = NamedDB("ws").toDB()

  describe("variables") {
    it("should be available") { implicit session =>
      val fg = FactoryGirl(Post)
      fg.factoriesDir = "foo bar baz"
      val post = fg.withVariables("name" -> "Kaz").create()
      post.title should equal("I just started this blog")
      post.body should equal("Hello, everyone! My name is Kaz. And bulah bulah...")
    }

    it("should be available with string interpolation") { implicit session =>
      val fg = FactoryGirl(Post, "post2")
      fg.factoriesDir = "foo bar baz"
      val post = fg.withVariables("name" -> "Kaz").create()
      post.title should equal("I just started this blog")
      post.body should not equal ("Hello, everyone! My name is Kaz. And bulah bulah... ${System.currentTimeMillis}")
    }

    it("should throw exception when key is absent") { implicit session =>
      intercept[IllegalStateException] {
        val fg = FactoryGirl(Post)
        fg.factoriesDir = "foo bar baz"
        try fg.create()
        catch {
          case e: Exception =>
            logger.info(s"Exception: ${e.getClass.getCanonicalName}", e)
            throw e
        }
      }
    }
  }

  describe("non-string values") {
    it("should be accepted") { implicit session =>
      intercept[Exception] {
        val fg = FactoryGirl(Post)
        fg.factoriesDir = "foo bar baz"
        fg.create("name" -> None) // should not accepted as 'None'
      }
    }
  }
}
