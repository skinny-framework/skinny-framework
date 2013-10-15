package skinny.orm

import scalikejdbc._, SQLInterpolation._
import skinny.util.JavaReflectAPI

/**
 * ActiveRecord::Base-like entity object base.
 *
 * {{{
 *   case class Company(id: Long, name: String) extends SkinnyResource[Company] {
 *     def skinnyCRUDMapper = Company
 *   }
 *   object Company extends SkinnyCRUDMapper[Company] {
 *     def extract(rs: WrappedResultSet, s: ResultName[Company]): Company = new Company(
 *       id = rs.longOpt(s.id),
 *       name = rs.string(s.name)
 *     )
 *   }
 *   // usage
 *   val company = Company.findById(id).get
 *   company.copy(name = "Oracle").save()
 *   company.destroy()
 * }}}
 *
 * @tparam Entity entity
 */
trait SkinnyRecordBase[Entity] {

  /**
   * Returns [[skinny.orm.SkinnyCRUDMapper]] for this SkinnyRecord.
   *
   * @return mapper
   */
  def skinnyCRUDMapper: SkinnyCRUDMapper[Entity]

  /**
   * Returns primary key search condition.
   *
   * @return sql part
   */
  def primaryKeyCondition: SQLSyntax

  /**
   * Saves this instance in DB. Notice: this methods only can update existing entity.
   *
   * If you need creation, define the primary key as an Option[Long] value, and use [[skinny.orm.MutableSkinnyRecord]] instead.
   *
   * @param session db session
   * @return self
   */
  def save()(implicit session: DBSession = skinnyCRUDMapper.autoSession): SkinnyRecordBase[Entity] = {
    skinnyCRUDMapper.updateBy(primaryKeyCondition).withNamedValues(attributesToPersist: _*)
    this
  }

  /**
   * Destroys this entity in DB.
   *
   * @param session db session
   * @return deleted count
   */
  def destroy()(implicit session: DBSession = skinnyCRUDMapper.autoSession): Int = {
    skinnyCRUDMapper.deleteBy(primaryKeyCondition)
  }

  /**
   * Returns attributes to persist.
   *
   * @return attributes
   */
  protected def attributesToPersist(): Seq[(SQLSyntax, Any)] = JavaReflectAPI.getterNames(this).map { name =>
    skinnyCRUDMapper.column.field(name) -> JavaReflectAPI.getter(this, name)
  }

}
