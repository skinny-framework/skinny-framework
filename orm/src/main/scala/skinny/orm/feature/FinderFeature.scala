package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Provides #find something APIs.
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
   * Finds a single entity by primary key.
   *
   * @param id id
   * @param s db session
   * @return single entity if exists
   */
  def findById(id: Id)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.eq(defaultAlias.field(primaryKeyFieldName), idToRawValue(id)).and(defaultScopeWithDefaultAlias)
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
      selectQueryWithAssociations.where.in(defaultAlias.field(primaryKeyFieldName), ids.map(idToRawValue)).and(defaultScopeWithDefaultAlias)
    }).list.apply())
  }

  /**
   * Finds all entities.
   *
   * @param s db session
   * @return entities
   */
  def findAll(ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(defaultScopeWithDefaultAlias).orderBy(ordering)
    }).list.apply())
  }

  /**
   * Finds all entities by paging.
   *
   * @param limit limit
   * @param offset offset
   * @param s db session
   * @return entities
   */
  def findAllPaging(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(defaultScopeWithDefaultAlias).orderBy(ordering).limit(limit).offset(offset)
    }).list.apply())
  }

  /**
   * Counts rows.
   *
   * @param s db session
   * @return count
   */
  def count(fieldName: Symbol = Symbol(primaryKeyFieldName), distinct: Boolean = false)(implicit s: DBSession = autoSession): Long = {
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
      select(count).from(as(defaultAlias)).where(defaultScopeWithDefaultAlias)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  /**
   * Counts distinct rows.
   *
   * @param s db session
   * @return distinct count
   */
  def distinctCount(fieldName: Symbol = Symbol(primaryKeyFieldName))(implicit s: DBSession = autoSession): Long = {
    count(fieldName, true)
  }

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
  def findAllBy(where: SQLSyntax, ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(where).and(defaultScopeWithDefaultAlias).orderBy(ordering)
    }).list.apply())
  }

  /**
   * Finds all entities by condition and paging.
   *
   * @param where where condition
   * @param limit limit
   * @param offset offset
   * @param s db session
   * @return entities
   */
  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(where).and(defaultScopeWithDefaultAlias)
        .orderBy(ordering).limit(limit).offset(offset)
    }).list.apply())
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
