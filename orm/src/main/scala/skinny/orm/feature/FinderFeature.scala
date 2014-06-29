package skinny.orm.feature

import scalikejdbc._
import skinny.Pagination
import skinny.orm.SkinnyMapperBase
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Provides #find something APIs.
 */
trait FinderFeature[Entity]
  extends FinderFeatureWithId[Long, Entity]

trait FinderFeatureWithId[Id, Entity]
    extends SkinnyMapperBase[Entity]
    with NoIdFinderFeature[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with JoinsFeature[Entity]
    with IdFeature[Id]
    with IncludesFeatureWithId[Id, Entity] {

  /**
   * Default ordering condition.
   */
  override def defaultOrdering: SQLSyntax = primaryKeyField

  /**
   * Finds a single entity by primary key.
   */
  def findById(id: Id)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.eq(primaryKeyField, idToRawValue(id)).and(defaultScopeWithDefaultAlias)
    }).single.apply())
  }

  /**
   * Finds all entities by several primary keys.
   */
  def findAllByIds(ids: Id*)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations
        .where.in(primaryKeyField, ids.map(idToRawValue)).and(defaultScopeWithDefaultAlias)
    }).list.apply())
  }

  override def findAll(orderings: Seq[SQLSyntax] = defaultOrderings)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      val sql = selectQueryWithAssociations.where(defaultScopeWithDefaultAlias)
      if (orderings.isEmpty) sql else sql.orderBy(sqls.csv(orderings: _*))
    }).list.apply())
  }

  override def findAllWithPagination(pagination: Pagination, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {
    if (hasManyAssociations.size > 0) {
      findAllWithLimitOffsetForOneToManyRelations(pagination.limit, pagination.offset, orderings)
    } else {
      findAllWithLimitOffset(pagination.limit, pagination.offset, orderings)
    }
  }

  override def findAllWithLimitOffset(limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) {
      findAllWithLimitOffsetForOneToManyRelations(limit, offset, orderings)
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        val sql = selectQueryWithAssociations.where(defaultScopeWithDefaultAlias)
        (if (orderings.isEmpty) sql else sql.orderBy(sqls.csv(orderings: _*))).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def findAllWithLimitOffsetForOneToManyRelations(limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {

    // find ids for pagination
    val ids: List[Any] = withSQL {
      if (orderings.isEmpty) singleSelectQuery.limit(limit).offset(offset)
      else {
        singleSelectQuery
          .orderBy(orderings.headOption.getOrElse(defaultOrdering))
          .limit(limit).offset(offset)
      }
    }.map(_.any(defaultAlias.resultName.field(primaryKeyFieldName))).list.apply()

    if (ids.isEmpty) {
      Nil
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        val sql = selectQueryWithAssociations.where(sqls.toAndConditionOpt(
          defaultScopeWithDefaultAlias,
          Some(sqls.in(defaultAlias.field(primaryKeyFieldName), ids))
        ))
        if (orderings.isEmpty) sql else sql.orderBy(sqls.csv(orderings: _*))
      }).list.apply())
    }
  }

  @deprecated("Use #findAllWithLimitOffset or #findAllWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllPaging(limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllWithLimitOffset(limit, offset, orderings)
  }

  override def findBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(sqls.toAndConditionOpt(Some(where), defaultScopeWithDefaultAlias))
    }).single.apply())
  }

  override def findAllBy(where: SQLSyntax, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      val sql = selectQueryWithAssociations
        .where(sqls.toAndConditionOpt(Some(where), defaultScopeWithDefaultAlias))
      if (orderings.isEmpty) sql else sql.orderBy(sqls.csv(orderings: _*))
    }).list.apply())
  }

  override def findAllByWithLimitOffset(where: SQLSyntax, limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) {
      findAllByWithLimitOffsetForOneToManyRelations(where, limit, offset, orderings)
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        val sql = selectQueryWithAssociations
          .where(sqls.toAndConditionOpt(Some(where), defaultScopeWithDefaultAlias))
        if (orderings.isEmpty) sql.limit(limit).offset(offset)
        else sql.orderBy(sqls.csv(orderings: _*)).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def findAllByWithLimitOffsetForOneToManyRelations(where: SQLSyntax, limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {

    // find ids for pagination
    val ids: List[Any] = withSQL {
      val sql = singleSelectQuery
        .where(sqls.toAndConditionOpt(Some(where), defaultScopeWithDefaultAlias))
      if (orderings.isEmpty) sql.limit(limit).offset(offset)
      else sql.orderBy(orderings.headOption.getOrElse(defaultOrdering)).limit(limit).offset(offset)
    }.map(_.any(defaultAlias.resultName.field(primaryKeyFieldName))).list.apply()

    if (ids.isEmpty) {
      Nil
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        val sql = selectQueryWithAssociations
          .where(sqls.toAndConditionOpt(
            Option(where),
            defaultScopeWithDefaultAlias,
            Some(sqls.in(defaultAlias.field(primaryKeyFieldName), ids))
          ))
        if (orderings.isEmpty) sql else sql.orderBy(sqls.csv(orderings: _*))
      }).list.apply())
    }
  }

  @deprecated("Use #findAllByWithLimitOffset or #findAllByWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = defaultOrderings)(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, limit, offset, orderings)
  }

}
