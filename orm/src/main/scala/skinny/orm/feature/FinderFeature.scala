package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny.Pagination
import skinny.orm.SkinnyMapperBase
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Provides #find something APIs.
 *
 * NOTE: For some reasons, skinny.orm.SkinnyJoinTable has copy implementation of this trait (subset).
 * Be aware that you should fix SkinnyJoinTable too.
 */
trait FinderFeature[Entity]
  extends FinderFeatureWithId[Long, Entity]

trait FinderFeatureWithId[Id, Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with JoinsFeature[Entity]
    with IdFeature[Id]
    with IncludesFeatureWithId[Id, Entity] {

  /**
   * Default ordering condition.
   */
  def defaultOrdering: SQLSyntax = primaryKeyField

  /**
   * Finds a single entity by primary key.
   *
   * @param id id
   * @param s db session
   * @return single entity if exists
   */
  def findById(id: Id)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.eq(primaryKeyField, idToRawValue(id)).and(defaultScopeWithDefaultAlias)
    }).single.apply())
  }

  /**
   * Finds all entities by several primary keys.
   *
   * @param ids several ids
   * @param s db session
   * @return entities
   */
  def findAllByIds(ids: Id*)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.in(primaryKeyField, ids.map(idToRawValue)).and(defaultScopeWithDefaultAlias)
    }).list.apply())
  }

  /**
   * Finds all entities.
   *
   * @param s db session
   * @return entities
   */
  def findAll(ordering: SQLSyntax = defaultOrdering)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(defaultScopeWithDefaultAlias).orderBy(ordering)
    }).list.apply())
  }

  /**
   * Finds all entities with pagination.
   */
  def findAllWithPagination(pagination: Pagination, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    if (hasManyAssociations.size > 0) {
      findAllWithLimitOffsetForOneToManyRelations(pagination.limit, pagination.offset, ordering)
    } else {
      findAllWithLimitOffset(pagination.limit, pagination.offset, ordering)
    }
  }

  /**
   * Finds all entities with pagination.
   */
  def findAllWithLimitOffset(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) {
      findAllWithLimitOffsetForOneToManyRelations(limit, offset, ordering)
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations.where(defaultScopeWithDefaultAlias).orderBy(ordering).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def findAllWithLimitOffsetForOneToManyRelations(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {

    // find ids for pagination
    val ids: List[Any] = withSQL {
      singleSelectQuery.limit(limit).offset(offset)
    }.map(_.any(defaultAlias.resultName.field(primaryKeyFieldName))).list.apply()

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations
        .where(sqls.toAndConditionOpt(
          defaultScopeWithDefaultAlias,
          Some(sqls.in(defaultAlias.field(primaryKeyFieldName), ids))
        )).orderBy(ordering)
    }).list.apply())
  }

  @deprecated("Use #findAllWithLimitOffset or #findAllWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllPaging(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllWithLimitOffset(limit, offset, ordering)
  }

  /**
   * Calculates rows.
   */
  def calculate(sql: SQLSyntax)(implicit s: DBSession = autoSession): BigDecimal = {
    withSQL {
      select(sql).from(as(defaultAlias)).where(defaultScopeWithDefaultAlias)
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
   * Finds an entity by condition.
   *
   * @param where where condition
   * @param s db session
   * @return single entity
   */
  def findBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(where).and(defaultScopeWithDefaultAlias)
    }).single.apply())
  }

  /**
   * Finds all entities by condition.
   *
   * @param where where condition
   * @param s db session
   * @return entities
   */
  def findAllBy(where: SQLSyntax, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(where).and(defaultScopeWithDefaultAlias).orderBy(ordering)
    }).list.apply())
  }

  /**
   * Finds all entities by condition and with pagination.
   */
  def findAllByWithPagination(where: SQLSyntax, pagination: Pagination, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, pagination.limit, pagination.offset, ordering)
  }

  /**
   * Finds all entities by condition and with pagination.
   */
  def findAllByWithLimitOffset(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) {
      findAllByWithLimitOffsetForOneToManyRelations(where, limit, offset, ordering)
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations.where(where).and(defaultScopeWithDefaultAlias).orderBy(ordering).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def findAllByWithLimitOffsetForOneToManyRelations(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {

    // find ids for pagination
    val ids: List[Any] = withSQL {
      singleSelectQuery.where(where).and(defaultScopeWithDefaultAlias).limit(limit).offset(offset)
    }.map(_.any(defaultAlias.resultName.field(primaryKeyFieldName))).list.apply()

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations
        .where(sqls.toAndConditionOpt(
          Option(where),
          defaultScopeWithDefaultAlias,
          Some(sqls.in(defaultAlias.field(primaryKeyFieldName), ids))
        )).orderBy(ordering)
    }).list.apply())
  }

  @deprecated("Use #findAllByWithLimitOffset or #findAllByWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, limit, offset, ordering)
  }

  /**
   * Counts all rows by condition.
   *
   * @param where where condition
   * @param s db session
   * @return entities
   */
  def countBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(defaultAlias)).where(where).and(defaultScopeWithDefaultAlias)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

}
