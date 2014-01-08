package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import scalikejdbc._, SQLInterpolation._

/**
 * Provides #withTableNmae APIs.
 */
trait DynamicTableNameFeature[Entity]
    extends DynamicTableNameFeatureWithId[Long, Entity] { self: SkinnyMapperBase[Entity] =>

}

trait DynamicTableNameFeatureWithId[Id, Entity] { self: SkinnyMapperBase[Entity] =>

  /**
   * Appends join definition on runtime.
   *
   * @param tableName table name
   * @return self
   */
  def withTableName(tableName: String): DynamicTableNameFeatureWithId[Id, Entity] with FinderFeatureWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] = {
    val _self = this
    val dynamicTableName = tableName

    new SkinnyMapperBase[Entity] with DynamicTableNameFeatureWithId[Id, Entity] with FinderFeatureWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] {
      override def defaultAlias = _self.defaultAlias
      override val tableName = dynamicTableName

      override def generateId = ???
      override def rawValueToId(rawValue: Any) = ???
      override def idToRawValue(id: Id) = id

      def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = _self.extract(rs, n)
    }
  }

}
