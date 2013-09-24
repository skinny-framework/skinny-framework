package skinny.orm

import scalikejdbc._, SQLInterpolation._

trait SkinnyJoinTable[Entity]
    extends SkinnyMapper[Entity] {

  override def extract(rs: WrappedResultSet, s: ResultName[Entity]): Entity = ???

  def findAll(limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      defaultSelectQuery.orderBy(syntax.id).limit(limit).offset(offset)
    }).list.apply()
  }

  def countAll()(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(syntax))
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def findAllBy(where: SQLSyntax, limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      defaultSelectQuery.where.append(where).orderBy(syntax.id).limit(limit).offset(offset)
    }).list.apply()
  }

  def countAllBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(syntax)).where.append(where)
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def createWithAttributes(strongParameters: PermittedStrongParameters)(implicit s: DBSession = autoSession): Unit = {
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

}
