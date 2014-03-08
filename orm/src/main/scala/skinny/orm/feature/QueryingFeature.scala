package skinny.orm.feature

import skinny.orm.{ Pagination, SkinnyMapperBase }
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Querying APIs feature.
 */
trait QueryingFeature[Entity] extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity] {

  sealed trait Calculation
  case object Sum extends Calculation
  case object Average extends Calculation
  case object Maximum extends Calculation
  case object Minimum extends Calculation

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
          case None => sqls.isNull(defaultAlias.field(key.name))
          case values: Seq[_] => sqls.in(defaultAlias.field(key.name), values)
          case value => sqls.eq(defaultAlias.field(key.name), value)
        }
    }
  )

  /**
   * Appends a raw where condition.
   *
   * @param condition
   * @return query builder
   */
  def where(condition: SQLSyntax): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(
    mapper = this, conditions = Seq(condition))

  /**
   * Appends pagination settings as limit/offset.
   *
   * @param pagination  pagination
   * @return query buildder
   */
  def paginate(pagination: Pagination): EntitiesSelectOperationBuilder = {
    new EntitiesSelectOperationBuilder(
      mapper = this, limit = Some(pagination.limit), offset = Some(pagination.offset))
  }

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
   * Appends order by condition.
   *
   * @param orderings orderings
   * @return query builder
   */
  def orderBy(orderings: SQLSyntax*): EntitiesSelectOperationBuilder = {
    new EntitiesSelectOperationBuilder(mapper = this, orderings = orderings)
  }

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
      orderings: Seq[SQLSyntax] = Nil,
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
      limit = limit,
      offset = offset
    )

    /**
     * Appends a raw where condition.
     *
     * @param condition
     * @return query builder
     */
    def where(condition: SQLSyntax): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(
      mapper = this.mapper,
      conditions = conditions ++ Seq(condition),
      limit = limit,
      offset = offset
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
      orderings: Seq[SQLSyntax] = Nil,
      limit: Option[Int] = None,
      offset: Option[Int] = None) extends SelectOperationBuilder(mapper, conditions, orderings, limit, offset, false) {

    /**
     * Appends pagination settings as limit/offset.
     *
     * @param pagination  pagination
     * @return query buildder
     */
    def paginate(pagination: Pagination): EntitiesSelectOperationBuilder = {
      this.copy(limit = Some(pagination.limit), offset = Some(pagination.offset))
    }

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
     * Calculates rows.
     */
    def calculate(sql: SQLSyntax)(implicit s: DBSession = autoSession): BigDecimal = {
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(sql).from(as(defaultAlias))
        conditions match {
          case Nil => q.where(defaultScopeWithDefaultAlias)
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }.and(defaultScopeWithDefaultAlias)
        }
      }.map(_.bigDecimal(1)).single.apply().map(_.toScalaBigDecimal).getOrElse(BigDecimal(0))
    }

    /**
     * Count only.
     */
    def count(fieldName: Symbol = Symbol(primaryKeyFieldName), distinct: Boolean = false)(implicit s: DBSession = autoSession): Long = {
      calculate {
        if (distinct) sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
        else sqls.count(defaultAlias.field(fieldName.name))
      }.toLong
    }

    /**
     * Counts distinct rows.
     */
    def distinctCount(fieldName: Symbol = Symbol(primaryKeyFieldName))(implicit s: DBSession = autoSession): Long = count(fieldName, true)

    /**
     * Calculates sum of a column.
     */
    def sum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.sum(defaultAlias.field(fieldName.name)))

    /**
     * Calculates average of a column.
     */
    def average(fieldName: Symbol, decimals: Option[Int] = None)(implicit s: DBSession = autoSession): BigDecimal = {
      calculate(decimals match {
        case Some(dcml) =>
          val decimalsValue = dcml match {
            case 1 => sqls"1"
            case 2 => sqls"2"
            case 3 => sqls"3"
            case 4 => sqls"4"
            case 5 => sqls"5"
            case 6 => sqls"6"
            case 7 => sqls"7"
            case 8 => sqls"8"
            case 9 => sqls"9"
            case _ => sqls"10"
          }
          sqls"round(${sqls.avg(defaultAlias.field(fieldName.name))}, ${decimalsValue})"
        case _ =>
          sqls.avg(defaultAlias.field(fieldName.name))
      })
    }
    def avg(fieldName: Symbol, decimals: Option[Int] = None)(implicit s: DBSession = autoSession): BigDecimal = average(fieldName, decimals)

    /**
     * Calculates minimum value of a column.
     */
    def minimum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.min(defaultAlias.field(fieldName.name)))
    def min(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = minimum(fieldName)

    /**
     * Calculates minimum value of a column.
     */
    def maximum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.max(defaultAlias.field(fieldName.name)))
    def max(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = maximum(fieldName)

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): List[Entity] = {
      implicit val repository = IncludesQueryRepository[Entity]()
      extract(withSQL {
        val query: SQLBuilder[Entity] = {
          conditions match {
            case Nil => defaultSelectQuery.where(defaultScopeWithDefaultAlias)
            case _ => conditions.tail.foldLeft(defaultSelectQuery.where(conditions.head)) {
              case (query, condition) => query.and.append(condition)
            }.and(defaultScopeWithDefaultAlias)
          }
        }

        def appendOrderingIfExists(query: SQLBuilder[Entity]) = {
          if (orderings.isEmpty) query
          else query.append(sqls"order by").append(sqls.csv(orderings: _*))
        }
        def appendPagingIfExists(query: SQLBuilder[Entity]) = {
          val paging = Seq(limit.map(l => sqls.limit(l)), offset.map(o => sqls.offset(o))).flatten
          paging.foldLeft(query) { case (query, part) => query.append(part) }
        }
        appendPagingIfExists(appendOrderingIfExists(query))

      }).list.apply()
    }

  }

}

trait QueryingFeatureWithId[Id, Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with IncludesFeatureWithId[Id, Entity] {

  sealed trait Calculation
  case object Sum extends Calculation
  case object Average extends Calculation
  case object Maximum extends Calculation
  case object Minimum extends Calculation

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
          case None => sqls.isNull(defaultAlias.field(key.name))
          case values: Seq[_] => sqls.in(defaultAlias.field(key.name), values)
          case value => sqls.eq(defaultAlias.field(key.name), value)
        }
    }
  )

  /**
   * Appends a raw where condition.
   *
   * @param condition
   * @return query builder
   */
  def where(condition: SQLSyntax): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(
    mapper = this, conditions = Seq(condition))

  /**
   * Appends pagination settings as limit/offset.
   *
   * @param pagination  pagination
   * @return query buildder
   */
  def paginate(pagination: Pagination): EntitiesSelectOperationBuilder = {
    new EntitiesSelectOperationBuilder(
      mapper = this, limit = Some(pagination.limit), offset = Some(pagination.offset))
  }

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
   * Select query builder.
   *
   * @param mapper mapper
   * @param conditions registered conditions
   * @param limit limit
   * @param offset offset
   */
  abstract class SelectOperationBuilder(
      mapper: QueryingFeatureWithId[Id, Entity],
      conditions: Seq[SQLSyntax] = Nil,
      orderings: Seq[SQLSyntax] = Nil,
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
      orderings = orderings,
      limit = limit,
      offset = offset
    )

    /**
     * Appends a raw where condition.
     *
     * @param condition
     * @return query builder
     */
    def where(condition: SQLSyntax): EntitiesSelectOperationBuilder = new EntitiesSelectOperationBuilder(
      mapper = this.mapper,
      conditions = conditions ++ Seq(condition),
      limit = limit,
      offset = offset
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
      mapper: QueryingFeatureWithId[Id, Entity],
      conditions: Seq[SQLSyntax] = Nil,
      orderings: Seq[SQLSyntax] = Nil,
      limit: Option[Int] = None,
      offset: Option[Int] = None) extends SelectOperationBuilder(mapper, conditions, orderings, limit, offset, false) {

    /**
     * Appends pagination settings as limit/offset.
     *
     * @param pagination  pagination
     * @return query buildder
     */
    def paginate(pagination: Pagination): EntitiesSelectOperationBuilder = {
      this.copy(limit = Some(pagination.limit), offset = Some(pagination.offset))
    }

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
     * Appends order by condition.
     *
     * @param orderings orderings
     * @return query builder
     */
    def orderBy(orderings: SQLSyntax*): EntitiesSelectOperationBuilder = this.copy(orderings = orderings)

    /**
     * Calculates rows.
     */
    def calculate(sql: SQLSyntax)(implicit s: DBSession = autoSession): BigDecimal = {
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(sql).from(as(defaultAlias))
        conditions match {
          case Nil => q.where(defaultScopeWithDefaultAlias)
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }.and(defaultScopeWithDefaultAlias)
        }
      }.map(_.bigDecimal(1)).single.apply().map(_.toScalaBigDecimal).getOrElse(BigDecimal(0))
    }

    /**
     * Count only.
     */
    def count(fieldName: Symbol = Symbol(primaryKeyFieldName), distinct: Boolean = false)(implicit s: DBSession = autoSession): Long = {
      calculate {
        if (distinct) sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
        else sqls.count(defaultAlias.field(fieldName.name))
      }.toLong
    }

    /**
     * Counts distinct rows.
     */
    def distinctCount(fieldName: Symbol = Symbol(primaryKeyFieldName))(implicit s: DBSession = autoSession): Long = count(fieldName, true)

    /**
     * Calculates sum of a column.
     */
    def sum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.sum(defaultAlias.field(fieldName.name)))

    /**
     * Calculates average of a column.
     */
    def average(fieldName: Symbol, decimals: Option[Int] = None)(implicit s: DBSession = autoSession): BigDecimal = {
      calculate(decimals match {
        case Some(dcml) =>
          val decimalsValue = dcml match {
            case 1 => sqls"1"
            case 2 => sqls"2"
            case 3 => sqls"3"
            case 4 => sqls"4"
            case 5 => sqls"5"
            case 6 => sqls"6"
            case 7 => sqls"7"
            case 8 => sqls"8"
            case 9 => sqls"9"
            case _ => sqls"10"
          }
          sqls"round(${sqls.avg(defaultAlias.field(fieldName.name))}, ${decimalsValue})"
        case _ =>
          sqls.avg(defaultAlias.field(fieldName.name))
      })
    }
    def avg(fieldName: Symbol, decimals: Option[Int] = None)(implicit s: DBSession = autoSession): BigDecimal = average(fieldName, decimals)

    /**
     * Calculates minimum value of a column.
     */
    def minimum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.min(defaultAlias.field(fieldName.name)))
    def min(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = minimum(fieldName)

    /**
     * Calculates minimum value of a column.
     */
    def maximum(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = calculate(sqls.max(defaultAlias.field(fieldName.name)))
    def max(fieldName: Symbol)(implicit s: DBSession = autoSession): BigDecimal = maximum(fieldName)

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

        def appendOrderingIfExists(query: SQLBuilder[Entity]) = {
          if (orderings.isEmpty) query
          else query.append(sqls"order by").append(sqls.csv(orderings: _*))
        }
        def appendPagingIfExists(query: SQLBuilder[Entity]) = {
          val paging = Seq(limit.map(l => sqls.limit(l)), offset.map(o => sqls.offset(o))).flatten
          paging.foldLeft(query) { case (query, part) => query.append(part) }
        }
        appendPagingIfExists(appendOrderingIfExists(query))

      }).list.apply())
    }

  }

}
