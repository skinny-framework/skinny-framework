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
    with SkinnyModel[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with DynamicTableNameFeature[Entity]
    with JoinsFeature[Entity]
    with IncludesFeature[Entity]
    with QueryingFeature[Entity]
    with FinderFeature[Entity]
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

  override def joins(associations: Association[_]*): CRUDFeature[Entity] = {
    val _self = this
    val _associations = associations
    val _belongsTo = associations.filter(_.isInstanceOf[BelongsToAssociation[Entity]]).map(_.asInstanceOf[BelongsToAssociation[Entity]])
    val _hasOne = associations.filter(_.isInstanceOf[HasOneAssociation[Entity]]).map(_.asInstanceOf[HasOneAssociation[Entity]])
    val _hasMany = associations.filter(_.isInstanceOf[HasManyAssociation[Entity]]).map(_.asInstanceOf[HasManyAssociation[Entity]])

    // creates new instance but ideally this should be more DRY & safe implementation
    new CRUDFeature[Entity] {
      override protected val underlying = _self
      override private[skinny] val belongsToAssociations = _self.belongsToAssociations ++ _belongsTo
      override private[skinny] val hasOneAssociations = _self.hasOneAssociations ++ _hasOne
      override private[skinny] val hasManyAssociations = _self.hasManyAssociations ++ _hasMany

      override val associations = _self.associations ++ _associations
      override val defaultJoinDefinitions = _self.defaultJoinDefinitions
      override val defaultBelongsToExtractors = _self.defaultBelongsToExtractors
      override val defaultHasOneExtractors = _self.defaultHasOneExtractors
      override val defaultOneToManyExtractors = _self.defaultOneToManyExtractors

      override def autoSession = underlying.autoSession
      override def connectionPoolName = underlying.connectionPoolName
      override def connectionPool = underlying.connectionPool

      def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = underlying.extract(rs, n)
    }
  }

  /**
   * Replaces table name on runtime.
   *
   * @param tableName table name
   * @return self
   */
  override def withTableName(tableName: String): CRUDFeature[Entity] = {
    val _self = this
    val dynamicTableName = tableName

    // creates new instance but ideally this should be more DRY & safe implementation
    new CRUDFeature[Entity] {
      // overwritten table name
      override val tableName = dynamicTableName

      override protected val underlying = _self
      override private[skinny] val belongsToAssociations = _self.belongsToAssociations
      override private[skinny] val hasOneAssociations = _self.hasOneAssociations
      override private[skinny] val hasManyAssociations = _self.hasManyAssociations
      override val associations = _self.associations
      override val defaultJoinDefinitions = _self.defaultJoinDefinitions
      override val defaultBelongsToExtractors = _self.defaultBelongsToExtractors
      override val defaultHasOneExtractors = _self.defaultHasOneExtractors
      override val defaultOneToManyExtractors = _self.defaultOneToManyExtractors
      override def autoSession = underlying.autoSession
      override def connectionPoolName = underlying.connectionPoolName
      override def connectionPool = underlying.connectionPool

      def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = underlying.extract(rs, n)
    }
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
   * Updates entities with parameters.
   *
   * @param id primary key
   * @param parameters parameters
   * @return updated count
   */
  def updateById(id: Long, parameters: PermittedStrongParameters): Int = updateById(id).withPermittedAttributes(parameters)

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
    private[this] val attributesToBeUpdated = new mutable.HashMap[SQLSyntax, Any]()

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
      attributesToBeUpdated.update(namedValue._1, namedValue._2)
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
          else xs.+=(column -> newValue)
      }.toSeq
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

  // for SkinnyModel

  override def createNewModel(parameters: PermittedStrongParameters) = createWithPermittedAttributes(parameters)

  override def findAllModels() = findAll()

  override def findModel(id: Long) = findById(id)

  override def updateModelById(id: Long, parameters: PermittedStrongParameters) = updateById(id, parameters)

  override def deleteModelById(id: Long) = deleteById(id)

}
