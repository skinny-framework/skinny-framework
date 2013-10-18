package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny._
import skinny.orm._
import skinny.orm.feature.associations._
import scala.collection.mutable
import skinny.orm.feature.associations.HasManyAssociation
import skinny.orm.feature.associations.BelongsToAssociation
import skinny.orm.feature.associations.HasOneAssociation

/**
 * Provides auto-generated CRUD feature.
 *
 * @tparam Entity entity
 */
trait CRUDFeature[Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with StrongParametersFeature {

  /**
   * The primary key should be an auto-increment value if true.
   */
  def useAutoIncrementPrimaryKey = true

  /**
   * Returns default scope for update/delete operations.
   *
   * @return default scope
   */
  def defaultScopeForUpdateOperations: Option[SQLSyntax] = None

  /**
   * Returns default scope for select queries.
   *
   * @return default scope
   */
  def defaultScopeWithDefaultAlias: Option[SQLSyntax] = None

  /**
   * Appends join definition on runtime.
   *
   * @param associations associations
   * @return self
   */
  def joins(associations: Association[_]*): CRUDFeatureWithAssociations[Entity] = {
    val belongsTo = associations.filter(_.isInstanceOf[BelongsToAssociation[Entity]]).map(_.asInstanceOf[BelongsToAssociation[Entity]])
    val hasOne = associations.filter(_.isInstanceOf[HasOneAssociation[Entity]]).map(_.asInstanceOf[HasOneAssociation[Entity]])
    val hasMany = associations.filter(_.isInstanceOf[HasManyAssociation[Entity]]).map(_.asInstanceOf[HasManyAssociation[Entity]])
    new CRUDFeatureWithAssociations[Entity](this, belongsTo, hasOne, hasMany)
  }

  /**
   * Returns select query builder.
   *
   * @return query builder
   */
  def selectQuery: SelectSQLBuilder[Entity] = defaultSelectQuery

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
      mapper: CRUDFeature[Entity],
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
   *
   * @param mapper mapper
   * @param conditions registered conditions
   * @param limit limit
   * @param offset offset
   */
  case class EntitiesSelectOperationBuilder(
      mapper: CRUDFeature[Entity],
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
      withExtractor(withSQL {
        val query: SQLBuilder[Entity] = {
          conditions match {
            case Nil => selectQuery.where(defaultScopeWithDefaultAlias)
            case _ => conditions.tail.foldLeft(selectQuery.where(conditions.head)) {
              case (query, condition) => query.and.append(condition)
            }.and(defaultScopeWithDefaultAlias)
          }
        }
        val paging = Seq(limit.map(l => sqls.limit(l)), offset.map(o => sqls.offset(o))).flatten
        paging.foldLeft(query) { case (query, part) => query.append(part) }
      }).list.apply()
    }

  }

  case class CountSelectOperationBuilder(
      mapper: CRUDFeature[Entity],
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

  /**
   * Finds a single entity by primary key.
   *
   * @param id id
   * @param s db session
   * @return single entity if exists
   */
  def findById(id: Long)(implicit s: DBSession = autoSession): Option[Entity] = {
    withExtractor(withSQL {
      selectQuery.where.eq(defaultAlias.field(primaryKeyName), id).and(defaultScopeWithDefaultAlias)
    }).single.apply()
  }

  /**
   * Finds all entities by several primary keys.
   *
   * @param ids several ids
   * @param s db session
   * @return entities
   */
  def findAllByIds(ids: Long*)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where.in(defaultAlias.field(primaryKeyName), ids).and(defaultScopeWithDefaultAlias)
    }).list.apply()
  }

  /**
   * Finds all entities.
   *
   * @param s db session
   * @return entities
   */
  def findAll()(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName))
    }).list.apply()
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
    withExtractor(withSQL {
      selectQuery.where(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName)).limit(limit).offset(offset)
    }).list.apply()
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
   * Finds all entities by condition.
   *
   * @param where where condition
   * @param s db session
   * @return entities
   */
  def findAllBy(where: SQLSyntax)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where(where).and(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName))
    }).list.apply()
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
    withExtractor(withSQL {
      selectQuery.where(where).and(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName)).limit(limit).offset(offset)
    }).list.apply()
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

  /**
   * Attributes to be inserted when creation.
   */
  private[this] val attributesForCreation = new mutable.LinkedHashSet[(SQLSyntax, Any)]()

  /**
   * Accepted factories for attributesForCreation.
   */
  private[this] val attributesForCreationFactories = new mutable.LinkedHashSet[() => Boolean]()

  /**
   * Adds new attribute to be inserted when creation.
   *
   * @param namedValue named value
   * @return self
   */
  protected def addAttributeForCreation(namedValue: => (SQLSyntax, Any)) = {
    acceptAttributeForCreation(namedValue)
    this
  }

  /**
   * Attributes for creation are ready if true
   */
  private[this] lazy val attributesForCreationReady: Boolean = {
    attributesForCreationFactories.foreach(_.apply())
    true
  }

  /**
   * Accepts an attribute for creation.
   *
   * @param namedValue named value
   */
  private[this] def acceptAttributeForCreation(namedValue: => (SQLSyntax, Any)): Unit = {
    attributesForCreationFactories.add(() => attributesForCreation.add(namedValue))
  }

  /**
   * Merges already registered attributes to be inserted and parameters.
   *
   * @param namedValues named values
   * @return merged attributes
   */
  protected def mergeNamedValuesForCreation(namedValues: Seq[(SQLSyntax, Any)]): Seq[(SQLSyntax, Any)] = {
    if (!attributesForCreationReady) {
      throw new IllegalStateException("Attributes for creation query is not ready!")
    }

    namedValues.foldLeft(attributesForCreation) {
      case (xs, (column, newValue)) =>
        if (xs.exists(_._1 == column)) xs.map { case (c, v) => if (c == column) (column -> newValue) else (c, v) }
        else xs + (column -> newValue)
    }
    val toBeInserted = attributesForCreation.++(namedValues).toSeq
    toBeInserted
  }

  /**
   * Extracts named values from the permitted parameters.
   *
   * @param strongParameters permitted parameters
   * @return named values
   */
  protected def namedValuesForCreation(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
    mergeNamedValuesForCreation(strongParameters.params.map {
      case (name, (value, paramType)) =>
        column.field(name) -> getTypedValueFromStrongParameter(name, value, paramType)
    }.toSeq)
  }

  /**
   * Creates a new entity with permitted strong parameters.
   *
   * @param strongParameters permitted parameters
   * @param s db session
   * @return created count
   */
  def createWithPermittedAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Long = {
    createWithNamedValues(namedValuesForCreation(strongParameters): _*)
  }

  /**
   * Creates a new entity with non-permitted parameters.
   *
   * CAUTION: If you use this method in some web apps, you might have mass assignment vulnerability.
   *
   * @param parameters parameters
   * @param s db session
   * @return created count
   */
  def createWithAttributes(parameters: (Symbol, Any)*)(implicit s: DBSession = autoSession): Long = {
    createWithNamedValues(mergeNamedValuesForCreation(parameters.map {
      case (name, value) => column.field(name.name) -> value
    }.toSeq): _*)
  }

  /**
   * #createWithNamedValues pre-execution.
   *
   * @param namedValues named values
   */
  protected def beforeCreate(namedValues: Seq[(SQLSyntax, Any)])(implicit s: DBSession = autoSession): Unit = {}

  /**
   * #createWithNamedValues post-execution.
   *
   * @param namedValues named values
   * @param generatedId generated id
   */
  protected def afterCreate(namedValues: Seq[(SQLSyntax, Any)], generatedId: Option[Long])(
    implicit s: DBSession = autoSession): Unit = {}

  /**
   * Creates a new entity with named values.
   *
   * @param namedValues named values
   * @param s db session
   * @return created count
   */
  def createWithNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Long = {
    val allNamedValues = mergeNamedValuesForCreation(namedValues)
    beforeCreate(allNamedValues)
    if (useAutoIncrementPrimaryKey) {
      val id = withSQL { insert.into(this).namedValues(allNamedValues: _*) }.updateAndReturnGeneratedKey.apply()
      afterCreate(allNamedValues, Some(id))
      id
    } else {
      withSQL { insert.into(this).namedValues(allNamedValues: _*) }.update.apply()
      val idOpt = allNamedValues.find(v => v._1 == column.field(primaryKeyName)).map {
        case (_, value) =>
          try value.toString.toLong
          catch { case e: Exception => 0L }
      }
      afterCreate(allNamedValues, idOpt)
      idOpt.getOrElse(-1L)
    }
  }

  /**
   * Returns update query builder with condition.
   *
   * @param where where condition
   * @return update query builder
   */
  def updateBy(where: SQLSyntax): UpdateOperationBuilder = {
    new UpdateOperationBuilder(this, where, beforeUpdateByHandlers.toSeq, afterUpdateByHandlers.toSeq)
  }

  /**
   * Returns update query builder with primary key.
   *
   * @param id primary key
   * @return update query builder
   */
  def updateById(id: Long): UpdateOperationBuilder = updateBy(byId(id))

  /**
   * Returns a query part which represents primary key search condition.
   *
   * @param id primary key
   * @return query part
   */
  protected def byId(id: Long) = sqls.eq(column.field(primaryKeyName), id)

  /**
   * #updateBy pre-execution handler.
   */
  type BeforeUpdateByHandler = (DBSession, SQLSyntax, Seq[(SQLSyntax, Any)]) => Unit

  /**
   * #updateBy post-execution handler.
   */
  type AfterUpdateByHandler = (DBSession, SQLSyntax, Seq[(SQLSyntax, Any)], Int) => Unit

  /**
   * Registered beforeUpdateByHandlers.
   */
  protected val beforeUpdateByHandlers = new scala.collection.mutable.ListBuffer[BeforeUpdateByHandler]

  /**
   * Registered afterUpdateByHandlers.
   */
  protected val afterUpdateByHandlers = new scala.collection.mutable.ListBuffer[AfterUpdateByHandler]

  /**
   * Registers #updateBy pre-execution handler.
   *
   * @param handler event handler
   */
  protected def beforeUpdateBy(handler: BeforeUpdateByHandler): Unit = beforeUpdateByHandlers.append(handler)

  /**
   * Registers #updateBy post-execution handler.
   *
   * @param handler event handler
   */
  protected def afterUpdateBy(handler: AfterUpdateByHandler): Unit = afterUpdateByHandlers.append(handler)

  /**
   * Update query builder/executor.
   *
   * @param mapper mapper
   * @param where condition
   */
  class UpdateOperationBuilder(
      mapper: CRUDFeature[Entity],
      where: SQLSyntax,
      beforeHandlers: Seq[BeforeUpdateByHandler],
      afterHandlers: Seq[AfterUpdateByHandler]) {

    /**
     * Attributes to be updated.
     */
    private[this] val attributesToBeUpdated = new mutable.LinkedHashSet[(SQLSyntax, Any)]()

    /**
     * Additional query parts after `set` keyword.
     */
    private[this] val additionalUpdateSQLs = new mutable.LinkedHashSet[SQLSyntax]()

    /**
     * Adds new attribute to be updated.
     *
     * @param namedValue named value
     * @return self
     */
    protected def addAttributeToBeUpdated(namedValue: (SQLSyntax, Any)): UpdateOperationBuilder = {
      attributesToBeUpdated.add(namedValue)
      this
    }

    /**
     * Adds new query part.
     *
     * @param queryPart query part
     * @return self
     */
    protected def addUpdateSQLPart(queryPart: SQLSyntax): UpdateOperationBuilder = {
      additionalUpdateSQLs.add(queryPart)
      this
    }

    /**
     * Converts permitted strong parameters to named values.
     *
     * @param strongParameters permitted parameters
     * @return named values
     */
    protected def toNamedValuesToBeUpdated(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
      strongParameters.params.map {
        case (name, (value, paramType)) =>
          column.field(name) -> getTypedValueFromStrongParameter(name, value, paramType)
      }.toSeq
    }

    /**
     * Merges already registered attributes to be updated and parameters.
     *
     * @param namedValues named values
     * @return merged attributes
     */
    protected def mergeNamedValues(namedValues: Seq[(SQLSyntax, Any)]): Seq[(SQLSyntax, Any)] = {
      namedValues.foldLeft(attributesToBeUpdated) {
        case (xs, (column, newValue)) =>
          if (xs.exists(_._1 == column)) xs.map { case (c, v) => if (c == column) (column -> newValue) else (c, v) }
          else xs + (column -> newValue)
      }
      val toBeUpdated = attributesToBeUpdated.++(namedValues).toSeq
      toBeUpdated
    }

    /**
     * Merges additional query parts.
     *
     * @param queryBuilder query builder
     * @param othersAreEmpty other attributes to be updated is empty if true
     * @return query builder
     */
    protected def mergeAdditionalUpdateSQLs(queryBuilder: UpdateSQLBuilder, othersAreEmpty: Boolean): UpdateSQLBuilder = {
      if (additionalUpdateSQLs.isEmpty) {
        queryBuilder
      } else {
        val updates = sqls.csv(additionalUpdateSQLs.toSeq: _*)
        if (othersAreEmpty) queryBuilder.append(updates)
        else queryBuilder.append(sqls", ${updates}")
      }
    }

    /**
     * Updates entities with these permitted strong parameters.
     *
     * @param strongParameters permitted strong parameters
     * @param s db session
     * @return updated count
     */
    def withPermittedAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Int = {
      withNamedValues(toNamedValuesToBeUpdated(strongParameters): _*)
    }

    /**
     * Updates entities with these non-permitted parameters.
     *
     * CAUTION: If you use this method in some web apps, you might have mass assignment vulnerability.
     *
     * @param parameters unsafe parameters
     * @param s db session
     * @return updated count
     */
    def withAttributes(parameters: (Symbol, Any)*)(implicit s: DBSession = autoSession): Int = {
      withNamedValues(parameters.map {
        case (name, value) => column.field(name.name) -> value
      }: _*)
    }

    /**
     * Updates entities with named values.
     *
     * @param namedValues named values
     * @param s db session
     * @return updated count
     */
    def withNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Int = {
      val allValues = mergeNamedValues(namedValues)
      beforeHandlers.foreach(_.apply(s, where, allValues))
      val updatedCount = withSQL {
        mergeAdditionalUpdateSQLs(update(mapper).set(allValues: _*), allValues.isEmpty)
          .where.append(where).and(defaultScopeForUpdateOperations)
      }.update.apply()
      afterHandlers.foreach(_.apply(s, where, allValues, updatedCount))
      updatedCount
    }

  }

  /**
   * Deletes entities by condition.
   *
   * @param where condition
   * @param s db session
   * @return deleted count
   */
  def deleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Int = {
    beforeDeleteBy(where)
    val count = withSQL {
      delete.from(this).where(where).and(defaultScopeForUpdateOperations)
    }.update.apply()
    afterDeleteBy(where, count)
  }

  /**
   * #deleteBy pre-execution.
   *
   * @param where condition
   */
  protected def beforeDeleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Unit = {}

  /**
   * #deleteBy post-execution.
   *
   * @param where condition
   * @param deletedCount deleted count
   * @return count
   */
  protected def afterDeleteBy(where: SQLSyntax, deletedCount: Int)(implicit s: DBSession = autoSession): Int = deletedCount

  /**
   * Deletes a single entity by primary key.
   *
   * @param id primary key
   * @param s db session
   * @return deleted count
   */
  def deleteById(id: Long)(implicit s: DBSession = autoSession): Int = deleteBy(byId(id))

}
