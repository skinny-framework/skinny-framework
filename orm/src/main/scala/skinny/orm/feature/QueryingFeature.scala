package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Querying APIs feature.
 */
trait QueryingFeature[Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with IncludesFeature[Entity] {

  /**
   * Appends where conditions.
   *
   * @param conditions
   * @return query builder
   */
  def where(conditions: (Symbol, Any)*): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(
    mapper = this,
    conditions = conditions.map {
      case (key, value) =>
        value match {
          case None => sqls.isNull(defaultAlias.field(key.name)) // TODO Null/NotNull
          case values: Seq[_] => sqls.in(defaultAlias.field(key.name), values)
          case value => sqls.eq(defaultAlias.field(key.name), value)
        }
    }
  )

  /**
   * Appends limit part.
   *
   * @param n value
   * @return query builder
   */
  def limit(n: Int): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(mapper = this, limit = Some(n))

  /**
   * Appends offset part.
   *
   * @param n value
   * @return query builder
   */
  def offset(n: Int): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(mapper = this, offset = Some(n))

  /**
   * Count only.
   *
   * @return query builder
   */
  def count(): CountSelectOperationBuilder = new CountSelectOperationBuilder(mapper = this)

  /**
   * Select query builder.
   *
   * @param mapper mapper
   * @param conditions registered conditions
   * @param limit limit
   * @param offset offset
   */
  abstract class SelectOperationBuilder(
      mapper: QueryingFeature[Entity],
      conditions: Seq[SQLSyntax] = Nil,
      limit: Option[Int] = None,
      offset: Option[Int] = None,
      isCountOnly: Boolean = false) {

    /**
     * Appends where conditions.
     *
     * @param additionalConditions conditions
     * @return query builder
     */
    def where(additionalConditions: (Symbol, Any)*): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(
      mapper = this.mapper,
      conditions = conditions ++ additionalConditions.map {
        case (key, value) =>
          value match {
            case values: Seq[_] => sqls.in(defaultAlias.field(key.name), values)
            case value => sqls.eq(defaultAlias.field(key.name), value)
          }
      },
      limit = None,
      offset = None
    )
  }

  /**
   * Entities finder builder.
   *
   * @param mapper mapper
   * @param conditions registered conditions
   * @param limit limit
   * @param offset offset
   */
  case class EntitiesSelectOperationBuilder(
      mapper: QueryingFeature[Entity],
      conditions: Seq[SQLSyntax] = Nil,
      limit: Option[Int] = None,
      offset: Option[Int] = None) extends SelectOperationBuilder(mapper, conditions, limit, offset, false) {

    /**
     * Appends limit part.
     *
     * @param n value
     * @return query builder
     */
    def limit(n: Int): EntitiesSelectOperationBuilder = this.copy(limit = Some(n))

    /**
     * Appends offset part.
     *
     * @param n value
     * @return query builder
     */
    def offset(n: Int): EntitiesSelectOperationBuilder = this.copy(offset = Some(n))

    /**
     * Count only.
     *
     * @return query builder
     */
    def count(): CountSelectOperationBuilder = CountSelectOperationBuilder(mapper, conditions)

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): List[Entity] = {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        val query: SQLBuilder[Entity] = {
          conditions match {
            case Nil => selectQueryWithAssociations.where(defaultScopeWithDefaultAlias)
            case _ => conditions.tail.foldLeft(selectQueryWithAssociations.where(conditions.head)) {
              case (query, condition) => query.and.append(condition)
            }.and(defaultScopeWithDefaultAlias)
          }
        }
        val paging = Seq(limit.map(l => sqls.limit(l)), offset.map(o => sqls.offset(o))).flatten
        paging.foldLeft(query) { case (query, part) => query.append(part) }
      }).list.apply())
    }

  }

  /**
   * Count operation builder.
   *
   * @param mapper mapper
   * @param conditions registered conditions
   */
  case class CountSelectOperationBuilder(
      mapper: QueryingFeature[Entity],
      conditions: Seq[SQLSyntax] = Nil) extends SelectOperationBuilder(mapper, conditions, None, None) {

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): Long = {
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(sqls.count).from(as(defaultAlias))
        conditions match {
          case Nil => q.where(defaultScopeWithDefaultAlias)
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }.and(defaultScopeWithDefaultAlias)
        }
      }.map(_.long(1)).single.apply().getOrElse(0L)
    }
  }

}
