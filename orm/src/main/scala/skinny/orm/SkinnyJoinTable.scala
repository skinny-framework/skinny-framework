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

  def findAll(orderings: Seq[SQLSyntax] = Seq(defaultOrdering))(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.orderBy(sqls.csv(orderings: _*))
    }).list.apply())
  }

  def findAllWithPagination(pagination: Pagination, orderings: Seq[SQLSyntax] = Seq(defaultOrdering))(
    implicit s: DBSession = autoSession): List[Entity] = {
    if (hasManyAssociations.size > 0) {
      findAllWithLimitOffsetForOneToManyRelations(pagination.limit, pagination.offset, orderings)
    } else {
      findAllWithLimitOffset(pagination.limit, pagination.offset, orderings)
    }
  }

  def findAllWithLimitOffset(limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = Seq(defaultOrdering))(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) {
      findAllWithLimitOffsetForOneToManyRelations(limit, offset, orderings)
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations
          .where(defaultScopeWithDefaultAlias)
          .orderBy(sqls.csv(orderings: _*)).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def findAllWithLimitOffsetForOneToManyRelations(limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = Seq(defaultOrdering))(
    implicit s: DBSession = autoSession): List[Entity] = {

    // find ids for pagination
    val ids: List[Any] = withSQL {
      singleSelectQuery
        .orderBy(orderings.headOption.getOrElse(defaultOrdering))
        .limit(limit).offset(offset)
    }.map(_.any(defaultAlias.resultName.field(primaryKeyFieldName))).list.apply()

    if (ids.isEmpty) {
      Nil
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations
          .where(sqls.toAndConditionOpt(
            defaultScopeWithDefaultAlias,
            Some(sqls.in(defaultAlias.field(primaryKeyFieldName), ids))
          )).orderBy(sqls.csv(orderings: _*))
      }).list.apply())
    }
  }

  @deprecated("Use #findAllWithLimitOffset or #findAllWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllPaging(limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = Seq(defaultOrdering))(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllWithLimitOffset(limit, offset, orderings)
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

  def findAllBy(where: SQLSyntax, orderings: Seq[SQLSyntax] = Seq(defaultAlias.field(primaryKeyFieldName)))(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.append(where).orderBy(sqls.csv(orderings: _*))
    }).list.apply())
  }

  def findAllByWithPagination(where: SQLSyntax, pagination: Pagination, orderings: Seq[SQLSyntax] = Seq(defaultAlias.field(primaryKeyFieldName)))(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, pagination.limit, pagination.offset, orderings)
  }

  def findAllByWithLimitOffset(where: SQLSyntax, limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = Seq(defaultAlias.field(primaryKeyFieldName)))(
    implicit s: DBSession = autoSession): List[Entity] = {

    if (hasManyAssociations.size > 0) {
      findAllByWithLimitOffsetForOneToManyRelations(where, limit, offset, orderings)
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations.where(sqls.toAndConditionOpt(Some(where), defaultScopeWithDefaultAlias))
          .orderBy(sqls.csv(orderings: _*)).limit(limit).offset(offset)
      }).list.apply())
    }
  }

  def findAllByWithLimitOffsetForOneToManyRelations(where: SQLSyntax, limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = Seq(defaultOrdering))(
    implicit s: DBSession = autoSession): List[Entity] = {

    // find ids for pagination
    val ids: List[Any] = withSQL {
      singleSelectQuery
        .orderBy(orderings.headOption.getOrElse(defaultOrdering))
        .limit(limit).offset(offset)
    }.map(_.any(defaultAlias.resultName.field(primaryKeyFieldName))).list.apply()

    if (ids.isEmpty) {
      Nil
    } else {
      implicit val repository = IncludesQueryRepository[Entity]()
      appendIncludedAttributes(extract(withSQL {
        selectQueryWithAssociations
          .where(sqls.toAndConditionOpt(
            Option(where),
            defaultScopeWithDefaultAlias,
            Some(sqls.in(defaultAlias.field(primaryKeyFieldName), ids))
          )).orderBy(sqls.csv(orderings: _*))
      }).list.apply())
    }
  }

  @deprecated("Use #findAllByWithLimitOffset or #findAllByWithPagination instead. This method will be removed since version 1.1.0.", since = "1.0.0")
  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0, orderings: Seq[SQLSyntax] = Seq(defaultAlias.field(primaryKeyFieldName)))(
    implicit s: DBSession = autoSession): List[Entity] = {
    findAllByWithLimitOffset(where, limit, offset, orderings)
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

  /**
   * Deletes entities by condition.
   *
   * @param where condition
   * @param s db session
   * @return deleted count
   */
  def deleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Int = {
    withSQL { delete.from(this).where(where) }.update.apply()
  }

  /**
   * Deletes all entities.
   */
  def deleteAll()(implicit s: DBSession = autoSession): Int = {
    withSQL { delete.from(this) }.update.apply()
  }

}
