package skinny.orm

import scalikejdbc._, SQLInterpolation._
import skinny._

/**
 * SkinnyMapper which represents join table which is used for associations.
 *
 * This mapper don't have primary key search and so on because they cannot work as expected or no need to implement.
 *
 * @tparam Entity entity
 */
trait SkinnyJoinTable[Entity] extends SkinnyMapper[Entity] {

  override def extract(rs: WrappedResultSet, s: ResultName[Entity]): Entity = ???

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
      mapper: SkinnyJoinTable[Entity],
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
      mapper: SkinnyJoinTable[Entity],
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
            case Nil => selectQuery
            case _ => conditions.tail.foldLeft(selectQuery.where(conditions.head)) {
              case (query, condition) => query.and.append(condition)
            }
          }
        }
        val paging = Seq(limit.map(l => sqls.limit(l)), offset.map(o => sqls.offset(o))).flatten
        paging.foldLeft(query) { case (query, part) => query.append(part) }
      }).list.apply()
    }

  }

  case class CountSelectOperationBuilder(
      mapper: SkinnyJoinTable[Entity],
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
          case Nil => q
          case _ => conditions.tail.foldLeft(q.where(conditions.head)) {
            case (query, condition) => query.and.append(condition)
          }
        }
      }.map(_.long(1)).single.apply().getOrElse(0L)
    }
  }

  def findAll()(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      defaultSelectQuery.orderBy(syntax.id)
    }).list.apply()
  }

  def findAllPaging(limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      defaultSelectQuery.orderBy(syntax.id).limit(limit).offset(offset)
    }).list.apply()
  }

  def countAll()(implicit s: DBSession = autoSession): Long = {
    withSQL {
      select(sqls.count).from(as(syntax))
    }.map(_.long(1)).single.apply().getOrElse(0L)
  }

  def findAllBy(where: SQLSyntax)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      defaultSelectQuery.where.append(where).orderBy(syntax.id)
    }).list.apply()
  }

  def findAllByPaging(where: SQLSyntax, limit: Int = 100, offset: Int = 0)(implicit s: DBSession = autoSession): List[Entity] = {
    withExtractor(withSQL {
      defaultSelectQuery.where.append(where).orderBy(syntax.id).limit(limit).offset(offset)
    }).list.apply()
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
