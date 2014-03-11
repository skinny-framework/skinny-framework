package skinny.session.jdbc

import skinny.orm._
import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime

case class ServletSession(jsessionId: String, skinnySessionId: Long, createdAt: DateTime) {

  def attachTo(session: SkinnySession)(implicit s: DBSession = ServletSession.autoSession) = {
    ServletSession.attachToSkinnySession(jsessionId, session)
  }

}

object ServletSession extends SkinnyTable[ServletSession] {
  override def tableName = "servlet_sessions"
  override def defaultAlias = createAlias("sv")
  override def defaultJoinColumnFieldName = "jsessionId"

  override def extract(rs: WrappedResultSet, n: ResultName[ServletSession]) = new ServletSession(
    jsessionId = rs.get(n.jsessionId),
    skinnySessionId = rs.get(n.skinnySessionId),
    createdAt = rs.get(n.createdAt))

  private[this] val sv = defaultAlias

  def findByJsessionId(jsessionId: String)(implicit s: DBSession = autoSession): Option[ServletSession] = withSQL {
    select.from(ServletSession as sv).where.eq(sv.jsessionId, jsessionId)
  }.map(rs => extract(rs, sv.resultName)).single.apply()

  def create(jsessionId: String, session: SkinnySession)(implicit s: DBSession = autoSession): Unit = withSQL {
    insert.into(this).namedValues(
      column.jsessionId -> jsessionId,
      column.skinnySessionId -> session.id,
      column.createdAt -> DateTime.now
    )
  }.update.apply()

  def attachToSkinnySession(jsessionId: String, session: SkinnySession)(implicit s: DBSession = autoSession): Unit = withSQL {
    update(ServletSession).set(column.skinnySessionId -> session.id).where.eq(column.jsessionId, jsessionId)
  }.update.apply()

  def deleteBySkinnySessionId(skinnySessionId: Long)(implicit s: DBSession = autoSession): Unit = withSQL {
    delete.from(ServletSession).where.eq(column.skinnySessionId, skinnySessionId)
  }.update.apply()

  def narrowDownAttachedServletSessions(session: SkinnySession, aliveCount: Int)(implicit s: DBSession = autoSession): Unit = {
    val jsessionIds = withSQL {
      select(sv.jsessionId).from(this as sv).where.eq(sv.skinnySessionId, session.id).orderBy(sv.createdAt).desc
    }.map(_.string(1)).list.apply().drop(aliveCount)

    if (!jsessionIds.isEmpty) {
      withSQL(delete.from(ServletSession).where.in(column.jsessionId, jsessionIds)).update.apply()
    }
  }

}
