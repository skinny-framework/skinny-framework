package skinny.orm

import scalikejdbc._, SQLInterpolation._
import skinny._
import skinny.orm.feature.QueryingFeature
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * SkinnyMapper which represents join table which is used for associations.
 *
 * This mapper don't have primary key search and so on because they cannot work as expected or no need to implement.
 *
 * @tparam Entity entity
 */
trait SkinnyJoinTable[Entity] extends SkinnyMapper[Entity] with QueryingFeature[Entity] {

  override def extract(rs: WrappedResultSet, s: ResultName[Entity]): Entity = ???

  def findAll(ordering: SQLSyntax = syntax.id)(implicit s: DBSession = autoSession): List[Entity] = {
    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.orderBy(ordering)
    }).list.apply())
  }

  def findAllPaging(limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = syntax.id)(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.orderBy(ordering).limit(limit).offset(offset)
    }).list.apply())
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

  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0, ordering: SQLSyntax = syntax.id)(
    implicit s: DBSession = autoSession): List[Entity] = {

    implicit val repository = IncludesQueryRepository[Entity]()
    appendIncludedAttributes(extract(withSQL {
      selectQueryWithAssociations.where.append(where).orderBy(ordering).limit(limit).offset(offset)
    }).list.apply())
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
