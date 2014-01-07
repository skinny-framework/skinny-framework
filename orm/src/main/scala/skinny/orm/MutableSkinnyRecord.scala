package skinny.orm

import scalikejdbc._, SQLInterpolation._

/**
 * ActiveRecord::Base-like entity object base.
 *
 * {{{
 *   case class Company(id: Option[Long], name: String) extends MutableSkinnyResource[Company] {
 *     def skinnyCRUDMapper = Company
 *   }
 *   object Company extends SkinnyCRUDMapper[Company] {
 *     def extract(rs: WrappedResultSet, s: ResultName[Company]): Company = new Company(
 *       id = rs.longOpt(s.id),
 *       name = rs.string(s.name)
 *     )
 *   }
 *   // usage
 *   val companyId = Company(name = "Sun").create()
 *   val company = Company.findById(companyId).get
 *   company.copy(name = "Oracle").save()
 *   company.destroy()
 * }}}
 *
 * @tparam Entity entity
 */
trait MutableSkinnyRecord[Entity] extends SkinnyRecordBase[Entity] {

  /**
   * Predicates this entity is new entity.
   *
   * @return true if not persisted
   */
  def isNewRecord: Boolean = id.isEmpty

  /**
   * Predicates this entity is NOT new entity.
   *
   * @return true if persisted
   */
  def isPersisted: Boolean = id.isDefined

  /**
   * Id as the primary key.
   *
   * If your entity's primary key is not single numeric value,
   * implement this method as a dummy(e.g. UnsupportedOperationException) override #primaryKeyCondition.
   *
   * @return id
   */
  def id: Option[Long]

  /**
   * Returns primary key search condition.
   *
   * @return sql part
   */
  override def primaryKeyCondition: SQLSyntax = {
    val pk = skinnyCRUDMapper.column.field(skinnyCRUDMapper.primaryKeyFieldName)
    sqls"${pk} = ${id}"
  }

  /**
   * Creates new entity in DB.
   *
   * @param session db session
   * @return generated primary key value
   */
  def create()(implicit session: DBSession = skinnyCRUDMapper.autoSession): Long = {
    skinnyCRUDMapper.createWithNamedValues(attributesToPersist: _*)
  }

}
