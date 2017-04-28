package skinny.orm

import skinny.orm.feature.CRUDFeatureWithId
import scalikejdbc._
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
 * @tparam Id id
 * @tparam Entity entity
 */
trait SkinnyRecordBaseWithId[Id, Entity] {

  /**
   * Returns [[skinny.orm.SkinnyCRUDMapperWithId]] for this SkinnyRecord.
   *
   * @return mapper
   */
  def skinnyCRUDMapper: CRUDFeatureWithId[Id, Entity]

  /**
   * Primary key
   */
  def id: Id

  /**
   * Saves this instance in DB. Notice: this methods only can update existing entity.
   *
   * @param session db session
   * @return self
   */
  def save()(implicit session: DBSession = skinnyCRUDMapper.autoSession): SkinnyRecordBaseWithId[Id, Entity] = {
    skinnyCRUDMapper.updateById(id).withNamedValues(attributesToPersist: _*)
    this
  }

  /**
   * Destroys this entity in DB.
   *
   * @param session db session
   * @return deleted count
   */
  def destroy()(implicit session: DBSession = skinnyCRUDMapper.autoSession): Int = {
    skinnyCRUDMapper.deleteById(id)
  }

  /**
   * Returns attribute names to be excluded when persistence.
   *
   * @return names
   */
  protected def excludedFieldNamesWhenSaving: Seq[String] = Nil

  /**
   * Returns attributes to persist.
   *
   * @return attributes
   */
  protected def attributesToPersist(): Seq[(SQLSyntax, Any)] = {
    JavaReflectAPI.getterNames(this)
      .filter { name => skinnyCRUDMapper.isValidFieldName(name) }
      .filterNot { name => excludedFieldNamesWhenSaving.contains(name) }
      .map { name =>
        (skinnyCRUDMapper.column.field(name), JavaReflectAPI.getter(this, name))
      }
  }

}
