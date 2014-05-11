package skinny.orm

import scalikejdbc._

/**
 * ActiveRecord::Base-like entity object base.
 *
 * {{{
 *   case class Company(id: Long, name: String) extends SkinnyResource[Company] {
 *     def skinnyCRUDMapper = Company
 *   }
 *   object Company extends SkinnyCRUDMapper[Long, Company] {
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
trait SkinnyRecordWithId[Id, Entity]
    extends SkinnyRecordBaseWithId[Id, Entity] {

  /**
   * Id as the primary key.
   *
   * If your entity's primary key is not single numeric value,
   * implement this method as a dummy(e.g. UnsupportedOperationException) override #primaryKeyCondition.
   *
   * @return id
   */
  def id: Id

}
