package skinny.orm

import scalikejdbc._, SQLInterpolation._

/**
 * SkinnyMapper base.
 *
 * @tparam Entity entity
 */
trait SkinnyMapperBase[Entity] extends SQLSyntaxSupport[Entity] {

  /**
   * Returns select query builder object to simply fetch rows without other joined tables.
   *
   * @return SQL builder object
   */
  protected def singleSelectQuery: SelectSQLBuilder[Entity] = select.from(as(defaultAlias))

  /**
   * Returns primary key name. (default: "id")
   *
   * Notice: Not a column name but field name. For example, not "first_name" but "firstName"
   *
   * @return primary key name
   */
  def primaryKeyName: String = "id"

  /**
   * Returns default table alias.
   *
   * @return default table alias
   */
  def defaultAlias: Alias[Entity] = syntax

  /**
   * Creates a new table alias for this mapper.
   *
   * @param name alias name
   * @return alias
   */
  def createAlias(name: String): Alias[Entity] = syntax(name)

  /**
   * Provides a code block with a table alias.
   *
   * @param op operation
   * @tparam A return type
   * @return result
   */
  def withAlias[A](op: Alias[Entity] => A): A = op(defaultAlias)

  /**
   * Provides a code block with a table alias.
   *
   * @param name table alias name
   * @param op operation
   * @tparam A return type
   * @return result
   */
  def withAlias[A](name: String)(op: Alias[Entity] => A): A = op(createAlias(name))

  /**
   * Provides a code block with the column name provider.
   *
   * @param op operation
   * @tparam A return type
   * @return result
   */
  def withColumns[A](op: ColumnName[Entity] => A): A = op(column)

  /**
   * Extracts entity from ResultSet.
   *
   * @param a table alias
   * @param rs result set
   * @return entity
   */
  def apply(a: Alias[Entity])(rs: WrappedResultSet): Entity = extract(rs, a.resultName)

  /**
   * Extracts entity from ResultSet.
   *
   * @param rs result set
   * @return entity
   */
  def apply(rs: WrappedResultSet): Entity = extract(rs, defaultAlias.resultName)

  /**
   * Extracts entity from ResultSet.
   *
   * @param rs result set
   * @param n result name
   * @return entity
   */
  def extract(rs: WrappedResultSet, n: ResultName[Entity]): Entity

}
