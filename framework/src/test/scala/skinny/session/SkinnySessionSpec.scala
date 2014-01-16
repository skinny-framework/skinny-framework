package skinny.session

import scalikejdbc._, SQLInterpolation._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import skinny.session.jdbc.SkinnySession
import org.joda.time.DateTime
import skinny.orm.feature._

class SkinnySessionSpec extends FunSpec with ShouldMatchers
    with Connection with CreateTables {

  val finderWithServletSessions: CRUDFeatureWithId[Long, SkinnySession] = {
    SkinnySession.joins(SkinnySession.servletSessionsRef)
  }

  describe("SkinnySession") {
    it("find or create skinny session") {
      val session1: SkinnySession = SkinnySession.findOrCreate("jsession1", None, DateTime.now.plusDays(10))
      // servletSessions attribute isn't refreshed for performance
      finderWithServletSessions.findById(session1.id).get.servletSessions.size should equal(1)

      val session2 = SkinnySession.findOrCreate("jsession1", Some("jsession2"), DateTime.now.plusDays(2))
      finderWithServletSessions.findById(session2.id).get.servletSessions.size should equal(2)
      session2.id should equal(session1.id)

      // set expireAt to past timestamp
      DB localTx { implicit s =>
        val c = SkinnySession.column
        withSQL(update(SkinnySession).set(c.expireAt -> DateTime.now.minusSeconds(10)).where.eq(c.id, session2.id))
          .update.apply()
      }

      val session3 = SkinnySession.findOrCreate("jsession2", Some("jsession3"), DateTime.now.plusSeconds(2))
      finderWithServletSessions.findById(session2.id).get.servletSessions.size should equal(1)
      session3.id should not equal (session2.id)
    }
  }

}
