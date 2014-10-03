package skinny.orm

import scalikejdbc._
import skinny.orm.feature._

/**
 * [deprecated] SkinnyMapper which represents join table which is used for associations.
 */
@deprecated("Use SkinnyMapper or SkinnyCRUDMapper instead because this mapper has ID.", since = "1.0.14")
trait SkinnyJoinTableWithId[Id, Entity]
    extends SkinnyMapperBase[Entity]
    with AssociationsFeature[Entity]
    with CRUDFeatureWithId[Id, Entity] {

  override def extract(rs: WrappedResultSet, s: ResultName[Entity]): Entity = {
    throw new IllegalStateException("You must implement this method if ResultSet extraction is needed.")
  }
}