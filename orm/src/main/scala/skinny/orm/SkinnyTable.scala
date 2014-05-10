package skinny.orm

import skinny.orm.feature._

/**
 * Table definition without single primary key specified by default.
 *
 * @tparam Entity entity
 */
@deprecated("Use SkinnyNoIdMapper or SkinnyMapper instead.", since = "1.0.14")
trait SkinnyTable[Entity] extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsWithIdFeature[Long, Entity]
    with QueryingFeature[Entity]
    with StrongParametersFeature {

  /**
   * Default column field name(camel case'd value such as promotionCode) when resolving associations.
   *
   * @return filed name
   */
  def defaultJoinColumnFieldName: String

  override def primaryKeyFieldName: String = defaultJoinColumnFieldName

  override protected def toDefaultForeignKeyName[A](mapper: AssociationsFeature[A]): String = {
    defaultJoinColumnFieldName
  }

}