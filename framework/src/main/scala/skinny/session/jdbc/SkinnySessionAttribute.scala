package skinny.session.jdbc

import skinny.orm._
import skinny.logging.LoggerProvider
import scalikejdbc._

case class SkinnySessionAttribute(
    skinnySessionId: Long,
    name: String,
    value: Option[Any],
    session: Option[SkinnySession] = None
) extends EntityEquality {

  def entityIdentity = (skinnySessionId, name)
}

object SkinnySessionAttribute
    extends SkinnyNoIdMapper[SkinnySessionAttribute]
    with LoggerProvider {

  override def tableName = "skinny_session_attributes"
  override def defaultAlias = createAlias("ska")
  override def nameConverters = Map("^name$" -> "attribute_name", "value$" -> "attribute_value")
  override def extract(rs: WrappedResultSet, n: ResultName[SkinnySessionAttribute]) = new SkinnySessionAttribute(
    skinnySessionId = rs.get(n.skinnySessionId), name = rs.get(n.name), value = rs.anyOpt(n.value)
  )

  def deleteBySkinnySessionId(skinnySessionId: Long)(implicit s: DBSession = autoSession): Unit = withSQL {
    delete.from(SkinnySessionAttribute).where.eq(column.skinnySessionId, skinnySessionId)
  }.update.apply()

}
