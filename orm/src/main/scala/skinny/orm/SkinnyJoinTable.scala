package skinny.orm

import scalikejdbc._, SQLInterpolation._
import skinny._
import skinny.orm.feature.QueryingFeatureWithId
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * SkinnyMapper which represents join table which is used for associations.
 *
 * This mapper don't have primary key search and so on because they cannot work as expected or no need to implement.
 *
 * @tparam Entity entity
 */
trait SkinnyJoinTable[Entity] extends SkinnyJoinTableWithId[Long, Entity] {
  override def rawValueToId(rawValue: Any) = rawValue.toString.toLong
  override def idToRawValue(id: Long) = id
}

trait SkinnyJoinTableWithId[Id, Entity]
    extends SkinnyMapperWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] {

  override def extract(rs: WrappedResultSet, s: ResultName[Entity]): Entity = {
    throw new IllegalStateException("You must implement this method if ResultSet extraction is needed.")
  }

  def defaultOrdering = defaultAlias.field(primaryKeyFieldName)

  def findAll(ordering: SQLSyntax = defaultOrdering)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.orderBy(ordering)
    }).list.apply())
  }

  def findAllWithPagination(pagination: Pagination, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    if (hasManyAssociations.size > 0) findAllWithLimitOffsetForOneToManyRelations(pagination.limit, pagination.offset, ordering)
    else findAllWithLimitOffset(pagination.limit, pagination.offset, ordering)
  }

  def findAllWithLimitOffset(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) findAllWithLimitOffsetForOneToManyRelations(limit, offset, ordering)
    else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations.where(defaultScopeWithDefaultAlias).orderBy(ordering).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def limitForOneToManyPagination(limit: Int): Int = 100 * limit

  def findAllWithLimitOffsetForOneToManyRelations(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    logger.debug("Since this operation has hash-many relationships, Skinny ORM will use #findAllWithLimitOffsetForOneToManyRelations. " +
      "Be aware that it's not an efficient way for pagination.")
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(defaultScopeWithDefaultAlias)
        .orderBy(ordering).limit(limitForOneToManyPagination(limit)).offset(0)
    }).list.apply()).drop(offset).take(limit)
  }

  @deprecated("Use #findAllWithLimitOffset or #findAllWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllPaging(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultOrdering)(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllWithLimitOffset(limit, offset, ordering)
  }

  def countAll()(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(syntax))
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def findBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Option[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.append(where)
    }).single.apply())
  }

  def findAllBy(where: SQLSyntax, ordering: SQLSyntax = syntax.id)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.append(where).orderBy(ordering)
    }).list.apply())
  }

  def findAllByWithPagination(where: SQLSyntax, pagination: Pagination, ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, pagination.limit, pagination.offset, ordering)
  }

  def findAllByWithLimitOffset(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where(where).and(defaultScopeWithDefaultAlias)
        .orderBy(ordering).limit(limit).offset(offset)
    }).list.apply())
  }

  @deprecated("Use #findAllByWithLimitOffset or #findAllByWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = defaultAlias.field(primaryKeyFieldName))(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, limit, offset, ordering)
  }

  def countAllBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(syntax)).where.append(where)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def createWithPermittedAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Unit = {
    withSQL {
      val values = strongParameters.params.map {
        case (name, (value, paramType)) =>
          column.field(name) -> getTypedValueFromStrongParameter(name, value, paramType)
      }.toSeq
      insert.into(this).namedValues(values: _*)
    }.update.apply()
  }

  def createWithNamedValues(namesAndValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Unit = {
    withSQL {
      insert.into(this).namedValues(namesAndValues: _*)
    }.update.apply()
  }

  def createWithAttributes(parameters: (Symbol, Any)*)(implicit s: DBSession = autoSession): Unit = {
    createWithNamedValues(parameters.map {
      case (name, value) => column.field(name.name) -> value
    }: _*)
  }

}
