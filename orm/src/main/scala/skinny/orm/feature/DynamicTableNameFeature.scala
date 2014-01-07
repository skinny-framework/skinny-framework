package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import scalikejdbc._, SQLInterpolation._

/**
 * Provides #withTableNmae APIs.
 */
trait DynamicTableNameFeature[Entity] { self: SkinnyMapperBase[Entity] =>

  /**
   * Appends join definition on runtime.
   *
   * @param tableName table name
   * @return self
   */
  def withTableName(tableName: String): DynamicTableNameFeature[Entity] with FinderFeature[Entity] with QueryingFeature[Entity] = {
    val _self = this
    val dynamicTableName = tableName

    new SkinnyMapperBase[Entity] with DynamicTableNameFeature[Entity] with FinderFeature[Entity] with QueryingFeature[Entity] {
      override def defaultAlias = _self.defaultAlias
      override val tableName = dynamicTableName
      def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = _self.extract(rs, n)
    }
  }

}
