package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny.orm._
import skinny.orm.feature.associations._
import scala.collection.mutable

trait CRUDFeature[Entity] extends BasicFeature[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with AssociationsFeature[Entity]
    with StrongParametersFeature {

  val useAutoIncrementPrimaryKey = true

  def defaultScopeWithoutAlias: Option[SQLSyntax] = None

  def defaultScopeWithDefaultAlias: Option[SQLSyntax] = None

  def joins(associations: Association[_]*): CRUDFeatureWithAssociations[Entity] = {
    val belongsTo = associations.filter(_.isInstanceOf[BelongsToAssociation[Entity]]).map(_.asInstanceOf[BelongsToAssociation[Entity]])
    val hasOne = associations.filter(_.isInstanceOf[HasOneAssociation[Entity]]).map(_.asInstanceOf[HasOneAssociation[Entity]])
    val hasMany = associations.filter(_.isInstanceOf[HasManyAssociation[Entity]]).map(_.asInstanceOf[HasManyAssociation[Entity]])
    new CRUDFeatureWithAssociations[Entity](this, belongsTo, hasOne, hasMany)
  }

  def selectQuery: SelectSQLBuilder[Entity] = defaultSelectQuery

  def findById(id: Long)(implicit s: DBSession = autoSession): Option[Entity] = {
    withExtractor(withSQL {
      selectQuery.where.eq(defaultAlias.field(primaryKeyName), id).and(defaultScopeWithDefaultAlias)
    }).single.apply()
  }

  def findAllByIds(ids: Long*)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where.in(defaultAlias.field(primaryKeyName), ids).and(defaultScopeWithDefaultAlias)
    }).list.apply()
  }

  def findAll(limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName)).limit(limit).offset(offset)
    }).list.apply()
  }

  def countAll()(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(defaultAlias)).where(defaultScopeWithDefaultAlias)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def findAllBy(where: SQLSyntax, limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where(where).and(defaultScopeWithDefaultAlias).orderBy(defaultAlias.field(primaryKeyName)).limit(limit).offset(offset)
    }).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(defaultAlias)).where(where).and(defaultScopeWithDefaultAlias)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def createWithAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Long = {
    if (useAutoIncrementPrimaryKey) {
      withSQL {
        insert.into(this).namedValues(namedValuesForCreation(strongParameters): _*)
      }.updateAndReturnGeneratedKey.apply()
    } else {
      withSQL {
        insert.into(this).namedValues(namedValuesForCreation(strongParameters): _*)
      }.update.apply()
      0L
    }
  }

  protected def namedValuesForCreation(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
    strongParameters.params.map {
      case (name, (value, paramType)) =>
        column.field(name) -> getTypedValueFromStrongParameter(name, value, paramType)
    }.toSeq
  }

  def createWithNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Long = {
    if (useAutoIncrementPrimaryKey) {
      withSQL { insert.into(this).namedValues(namedValues: _*) }.updateAndReturnGeneratedKey.apply()

    } else {
      withSQL { insert.into(this).namedValues(namedValues: _*) }.update.apply()

      namedValues.find(v => v._1.value == column.field(primaryKeyName).value).map {
        case (_, value) =>
          try value.toString.toLong
          catch { case e: Exception => 0L }
      }.getOrElse(0L)
    }
  }

  def updateBy(where: SQLSyntax): UpdateOperationBuilder = {
    new UpdateOperationBuilder(this, where)
  }

  def updateById(id: Long): UpdateOperationBuilder = {
    new UpdateOperationBuilder(this, byId(id))
  }

  protected def byId(id: Long) = sqls.eq(column.field(primaryKeyName), id)

  class UpdateOperationBuilder(self: CRUDFeature[Entity], where: SQLSyntax) {

    private[this] val attributesToBeUpdated = new mutable.LinkedHashSet[(SQLSyntax, Any)]()

    protected def addAttributeToBeUpdated(namedValue: (SQLSyntax, Any)): UpdateOperationBuilder = {
      attributesToBeUpdated.add(namedValue)
      this
    }

    protected def toNamedValuesToBeUpdated(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
      strongParameters.params.map {
        case (name, (value, paramType)) =>
          column.field(name) -> getTypedValueFromStrongParameter(name, value, paramType)
      }.toSeq
    }

    protected def mergeNamedValues(namedValues: Seq[(SQLSyntax, Any)]): Seq[(SQLSyntax, Any)] = {
      namedValues.foldLeft(attributesToBeUpdated) {
        case (xs, (column, newValue)) =>
          if (xs.exists(_._1.value == column.value)) xs.map { case (c, v) => if (c.value == column.value) (column -> newValue) else (c, v) }
          else xs + (column -> newValue)
      }
      val toBeUpdated = attributesToBeUpdated.++(namedValues).toSeq
      toBeUpdated
    }

    def withAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Unit = {
      withSQL {
        update(self)
          .set(mergeNamedValues(toNamedValuesToBeUpdated(strongParameters)): _*)
          .where(where).and(defaultScopeWithoutAlias)
      }.update.apply()
    }

    def withNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Unit = {
      withSQL {
        update(self).set(mergeNamedValues(namedValues): _*).where.append(where).and(defaultScopeWithoutAlias)
      }.update.apply()
    }
  }

  def deleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(this).where(where).and(defaultScopeWithoutAlias)
    }.update.apply()
  }

  def deleteById(id: Long)(implicit s: DBSession = autoSession): Unit = deleteBy(byId(id))

}
