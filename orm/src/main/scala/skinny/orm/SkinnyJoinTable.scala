package skinny.orm

import scalikejdbc._
import skinny.orm.feature._

/**
 * SkinnyMapper which represents join table which is used for associations.
 *
 * This mapper don't have primary key search and so on because they cannot work as expected or no need to implement.
 *
 * @tparam Entity entity
 */
trait SkinnyJoinTable[Entity]
    extends SkinnyMapperBase[Entity]
    with AssociationsFeature[Entity]
    with NoIdCUDFeature[Entity]
    with NoIdQueryingFeature[Entity]
    with NoIdFinderFeature[Entity] {

  override def extract(rs: WrappedResultSet, s: ResultName[Entity]): Entity = {
    throw new IllegalStateException("You must implement this method if ResultSet extraction is needed.")
  }

}
