package skinny.session.jdbc

import skinny.orm._
import scalikejdbc._, SQLInterpolation._

case class ServletSession(jsessionId: String, skinnySessionId: Long)

object ServletSession extends SkinnyTable[ServletSession] {
  override def tableName = "servlet_sessions"
  override def defaultAlias = createAlias("sv")
  override def defaultJoinColumnFieldName = "jsessionId"
  override def extract(rs: WrappedResultSet, n: ResultName[ServletSession]) = new ServletSession(
    jsessionId = rs.get(n.jsessionId), skinnySessionId = rs.get(n.skinnySessionId))
}
