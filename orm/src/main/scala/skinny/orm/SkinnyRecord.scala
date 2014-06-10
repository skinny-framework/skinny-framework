package skinny.orm

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
 * @tparam Entity entity
 */
trait SkinnyRecord[Entity]
  extends SkinnyRecordWithId[Long, Entity]
