package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Provides #find something APIs.
 */
trait FinderFeature[Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with JoinsFeature[Entity]
    with IncludesFeature[Entity] {

  /**
   * Finds a single entity by primary key.
   *
   * @param id id
   * @param s db session
   * @return single entity if exists
   */
  def findById(id: Long)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where.eq(defaultAlias.field(primaryKeyName), id).and(defaultScopeWithDefaultAlias)
    }).single.apply())
  }

  /**
   * Finds all entities by several primary keys.
   *
   * @param ids several ids
   * @param s db session
   * @return entities
   */
  def findAllByIds(ids: Long*)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where.in(defaultAlias.field(primaryKeyName), ids).and(defaultScopeWithDefaultAlias)
    }).list.apply())
  }

  /**
   * Finds all entities.
   *
   * @param s db session
   * @return entities
   */
  def findAll()(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName))
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
  def findAllPaging(limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName)).limit(limit).offset(offset)
    }).list.apply())
  }

  /**
   * Counts all rows.
   *
   * @param s db session
   * @return count
   */
  def countAll()(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(defaultAlias)).where(defaultScopeWithDefaultAlias)
    }.map(_.long(1)).single.apply().getOrElse(0L)
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
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where(where).and(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName))
    }).single.apply())
  }

  /**
   * Finds all entities by condition.
   *
   * @param where where condition
   * @param s db session
   * @return entities
   */
  def findAllBy(where: SQLSyntax)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where(where).and(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName))
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
  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    withIncludedAttributes(withExtractor(withSQL {
      defaultSelectQuery.where(where).and(defaultScopeWithDefaultAlias)
        .orderBy(defaultAlias.field(primaryKeyName))
        .limit(limit).offset(offset)
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
