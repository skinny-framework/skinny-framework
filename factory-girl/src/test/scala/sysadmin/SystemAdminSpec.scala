package sysadmin

import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.logging.Logging
import skinny.test.FactoryGirl

class userAdminSpec
    extends fixture.FunSpec
    with Matchers
    with Connection
    with CreateTables
    with AutoRollback
    with Logging {

  override def db(): DB = NamedDB('sysadmin).toDB()

  describe("factory.conf") {
    it("should be available") { implicit session =>
      val user = FactoryGirl(User).create()
      user.os should equal("Windows 8")
      user.java should equal("6")
      user.user should equal("sera")
    }
  }

  describe("with os/java/user attributes") {
    it("should be available") { implicit session =>
      val user = FactoryGirl(User).withAttributes('os -> "MacOS X", 'java -> "8", 'user -> "sera").create()
      user.os should equal("MacOS X")
      user.java should equal("8")
      user.user should equal("sera")
    }
  }
}
