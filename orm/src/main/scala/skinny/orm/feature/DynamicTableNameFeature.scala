package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import scalikejdbc._, SQLInterpolation._
import skinny.SkinnyModel

/**
 * Provides #withTableNmae APIs.
 */
trait DynamicTableNameFeature[Entity]
    extends DynamicTableNameFeatureWithId[Long, Entity] { self: SkinnyMapperBase[Entity] with IdFeature[Long] =>

}

trait DynamicTableNameFeatureWithId[Id, Entity] { self: SkinnyMapperBase[Entity] with IdFeature[Id] =>

  /**
   * Appends join definition on runtime.
   *
   * @param tableName table name
   * @return self
   */
  def withTableName(tableName: String): DynamicTableNameFeatureWithId[Id, Entity] with FinderFeatureWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] = {
    val _self = this
    val dynamicTableName = tableName

    new SkinnyMapperBase[Entity] with IdFeature[Id] with DynamicTableNameFeatureWithId[Id, Entity] with FinderFeatureWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] {
      override def defaultAlias = _self.defaultAlias
      override val tableName = dynamicTableName

      override def rawValueToId(value: Any) = _self.rawValueToId(value)
      override def idToRawValue(id: Id) = _self.idToRawValue(id)

      def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = _self.extract(rs, n)
    }
  }

}
