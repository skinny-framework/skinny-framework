package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny.orm._
import skinny.orm.feature.associations._

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
      selectQuery.where.eq(defaultAlias.id, id).and(defaultScopeWithDefaultAlias)
    }).single.apply()
  }

  def findAllByIds(ids: Long*)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where.in(defaultAlias.id, ids).and(defaultScopeWithDefaultAlias)
    }).list.apply()
  }

  def findAll(limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where(defaultScopeWithDefaultAlias).orderBy(defaultAlias.id).limit(limit).offset(offset)
    }).list.apply()
  }

  def countAll()(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(defaultAlias)).where(defaultScopeWithDefaultAlias)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def findAllBy(where: SQLSyntax, limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      selectQuery.where(where).and(defaultScopeWithDefaultAlias).orderBy(defaultAlias.id).limit(limit).offset(offset)
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
      0L
    }
  }

  def updateById(id: Long): UpdateOperationBuilder = new UpdateOperationBuilder(this, id)

  class UpdateOperationBuilder(self: CRUDFeature[Entity], id: Long) {

    def withAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Unit = {
      withSQL {
        val byId = sqls.eq(column.id, id)
        update(self).set(namedValuesForUpdate(strongParameters): _*).where.append(byId).and(defaultScopeWithoutAlias)
      }.update.apply()
    }

    protected def namedValuesForUpdate(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
      strongParameters.params.map {
        case (name, (value, paramType)) =>
          column.field(name) -> getTypedValueFromStrongParameter(name, value, paramType)
      }.toSeq
    }

    def withNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Unit = {
      withSQL {
        val byId = sqls.eq(column.id, id)
        update(self).set(namedValues: _*).where.append(byId).and(defaultScopeWithoutAlias)
      }.update.apply()
    }
  }

  def deleteById(id: Long)(implicit s: DBSession = autoSession): Unit = {
    withSQL {
      delete.from(this).where.eq(column.id, id).and(defaultScopeWithoutAlias)
    }.update.apply()
  }

}
