package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
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
    def count(fieldName: Symbol = Symbol(primaryKeyFieldName), distinct: Boolean = false): CountSelectOperationBuilder = {
      CountSelectOperationBuilder(mapper, fieldName, distinct, conditions)
    }
    def count: CountSelectOperationBuilder = count()

    /**
     * Calculates sum of a column.
     *
     * @return query builder
     */
    def sum(fieldName: Symbol): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Sum, fieldName, conditions)
    }

    /**
     * Calculates average of a column.
     *
     * @return query builder
     */
    def average(fieldName: Symbol, decimals: Option[Int] = None): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Average, fieldName, conditions, decimals)
    }
    def avg(fieldName: Symbol, decimals: Option[Int] = None): CalculationSelectOperationBuilder = average(fieldName, decimals)

    /**
     * Calculates minimum value of a column.
     *
     * @return query builder
     */
    def minimum(fieldName: Symbol): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Minimum, fieldName, conditions)
    }
    def min(fieldName: Symbol): CalculationSelectOperationBuilder = minimum(fieldName)

    /**
     * Calculates minimum value of a column.
     *
     * @return query builder
     */
    def maximum(fieldName: Symbol): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Maximum, fieldName, conditions)
    }
    def max(fieldName: Symbol): CalculationSelectOperationBuilder = maximum(fieldName)

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
        val paging = Seq(limit.map(l => sqls.limit(l)), offset.map(o => sqls.offset(o))).flatten
        paging.foldLeft(query) { case (query, part) => query.append(part) }
      }).list.apply()
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
      fieldName: Symbol,
      distinct: Boolean,
      conditions: Seq[SQLSyntax] = Nil) extends SelectOperationBuilder(mapper, conditions, None, None) {

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): BigDecimal = {
      val count = {
        if (distinct) {
          if (fieldName == Symbol(primaryKeyFieldName)) sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
          else sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
        } else {
          if (fieldName == Symbol(primaryKeyFieldName)) sqls.count(defaultAlias.field(fieldName.name))
          else sqls.count(defaultAlias.field(fieldName.name))
        }
      }
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(count).from(as(defaultAlias))
        conditions match {
          case Nil => q.where(defaultScopeWithDefaultAlias)
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }.and(defaultScopeWithDefaultAlias)
        }
      }.map(_.bigDecimal(1)).single.apply().map(_.toScalaBigDecimal).getOrElse(BigDecimal(0))
    }
  }

  /**
   * Sum operation builder.
   *
   * @param mapper mapper
   * @param conditions registered conditions
   */
  case class CalculationSelectOperationBuilder(
      mapper: QueryingFeature[Entity],
      calculation: Calculation,
      fieldName: Symbol,
      conditions: Seq[SQLSyntax] = Nil,
      decimals: Option[Int] = None) extends SelectOperationBuilder(mapper, conditions, None, None) {

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): Long = {
      val calc = calculation match {
        case Average =>
          decimals match {
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
          }
        case Minimum => sqls.min(defaultAlias.field(fieldName.name))
        case Maximum => sqls.max(defaultAlias.field(fieldName.name))
        case Sum => sqls.sum(defaultAlias.field(fieldName.name))
      }
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(calc).from(as(defaultAlias))
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
      mapper: QueryingFeatureWithId[Id, Entity],
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
    def count(fieldName: Symbol = Symbol(primaryKeyFieldName), distinct: Boolean = false): CountSelectOperationBuilder = {
      CountSelectOperationBuilder(mapper, fieldName, distinct, conditions)
    }
    def count: CountSelectOperationBuilder = count()

    /**
     * Calculates sum of a column.
     *
     * @return query builder
     */
    def sum(fieldName: Symbol): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Sum, fieldName, conditions)
    }

    /**
     * Calculates average of a column.
     *
     * @return query builder
     */
    def average(fieldName: Symbol, decimals: Option[Int] = None): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Average, fieldName, conditions, decimals)
    }
    def avg(fieldName: Symbol, decimals: Option[Int] = None): CalculationSelectOperationBuilder = average(fieldName, decimals)

    /**
     * Calculates minimum value of a column.
     *
     * @return query builder
     */
    def minimum(fieldName: Symbol): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Minimum, fieldName, conditions)
    }
    def min(fieldName: Symbol): CalculationSelectOperationBuilder = minimum(fieldName)

    /**
     * Calculates minimum value of a column.
     *
     * @return query builder
     */
    def maximum(fieldName: Symbol): CalculationSelectOperationBuilder = {
      CalculationSelectOperationBuilder(mapper, Maximum, fieldName, conditions)
    }
    def max(fieldName: Symbol): CalculationSelectOperationBuilder = maximum(fieldName)

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
      mapper: QueryingFeatureWithId[Id, Entity],
      fieldName: Symbol,
      distinct: Boolean,
      conditions: Seq[SQLSyntax] = Nil) extends SelectOperationBuilder(mapper, conditions, None, None) {

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): Long = {
      val count = {
        if (distinct) {
          if (fieldName == Symbol(primaryKeyFieldName)) sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
          else sqls.count(sqls.distinct(defaultAlias.field(fieldName.name)))
        } else {
          if (fieldName == Symbol(primaryKeyFieldName)) sqls.count(defaultAlias.field(fieldName.name))
          else sqls.count(defaultAlias.field(fieldName.name))
        }
      }
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(count).from(as(defaultAlias))
        conditions match {
          case Nil => q.where(defaultScopeWithDefaultAlias)
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }.and(defaultScopeWithDefaultAlias)
        }
      }.map(_.long(1)).single.apply().getOrElse(0L)
    }
  }

  /**
   * Sum operation builder.
   *
   * @param mapper mapper
   * @param conditions registered conditions
   */
  case class CalculationSelectOperationBuilder(
      mapper: QueryingFeatureWithId[Id, Entity],
      calculation: Calculation,
      fieldName: Symbol,
      conditions: Seq[SQLSyntax] = Nil,
      decimals: Option[Int] = None) extends SelectOperationBuilder(mapper, conditions, None, None) {

    /**
     * Actually applies SQL to the DB.
     *
     * @param session db session
     * @return query results
     */
    def apply()(implicit session: DBSession = autoSession): BigDecimal = {
      val calc = calculation match {
        case Average =>
          decimals match {
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
          }
        case Minimum => sqls.min(defaultAlias.field(fieldName.name))
        case Maximum => sqls.max(defaultAlias.field(fieldName.name))
        case Sum => sqls.sum(defaultAlias.field(fieldName.name))
      }
      withSQL {
        val q: SelectSQLBuilder[Entity] = select(calc).from(as(defaultAlias))
        conditions match {
          case Nil => q.where(defaultScopeWithDefaultAlias)
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }.and(defaultScopeWithDefaultAlias)
        }
      }.map(_.bigDecimal(1)).single.apply().map(_.toScalaBigDecimal).getOrElse(BigDecimal(0))
    }
  }

}
