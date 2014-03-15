package blog

import scalikejdbc._, SQLInterpolation._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import skinny.test.FactoryGirl

class BlogSpec extends fixture.FunSpec with ShouldMatchers
    with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB('fg).toDB()

  describe("variables") {
    it("should be available") { implicit session =>
      val post = FactoryGirl(Post).withVariables('name -> "Kaz").create()
      post.title should equal("I just started this blog")
      post.body should equal("Hello, everyone! My name is Kaz. And bulah bulah...")
    }

    it("should be available when key is absent") { implicit session =>
      val post = FactoryGirl(Post).create()
      post.title should equal("I just started this blog")
      post.body should equal("Hello, everyone! My name is #{name}. And bulah bulah...")
    }

  }
}
