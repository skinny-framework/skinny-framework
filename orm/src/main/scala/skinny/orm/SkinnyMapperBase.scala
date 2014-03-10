package skinny.orm

import scalikejdbc._, SQLInterpolation._

/**
 * SkinnyMapper base.
 *
 * @tparam Entity entity
 */
trait SkinnyMapperBase[Entity] extends SQLSyntaxSupport[Entity] {

  private[this] val _tableName = super.tableName
  private[this] val _columnNames = super.columnNames
  private[this] val _self: SkinnyMapperBase[Entity] = this

  protected def underlying: SkinnyMapperBase[Entity] = new SkinnyMapperBase[Entity] {
    override def defaultAlias = _self.defaultAlias
    override val tableName = _tableName
    override val columnNames = _columnNames
    def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = _self.extract(rs, n)
  }

  /**
   * Returns select query builder object to simply fetch rows without other joined tables.
   *
   * @return SQL builder object
   */
  protected def singleSelectQuery: SelectSQLBuilder[Entity] = select.from(as(defaultAlias))

  /**
   * Returns select query builder.
   *
   * @return query builder
   */
  def defaultSelectQuery: SelectSQLBuilder[Entity] = singleSelectQuery

  /**
   * Returns default scope for select queries.
   *
   * @return default scope
   */
  def defaultScope(alias: Alias[Entity]): Option[SQLSyntax] = None

  def defaultScopeWithDefaultAlias: Option[SQLSyntax] = defaultScope(defaultAlias)

  /**
   * Returns primary key name. (default: "id")
   *
   * Notice: Not a column name but field name. For example, not "first_name" but "firstName"
   *
   * @return primary key name
   */
  def primaryKeyFieldName: String = "id"

  /**
   * Returns default table alias.
   *
   * This method is abstract and should be implemented with unique name.
   *
   * override def defaultAlias = createAlias("sm")
   *
   * @return default table alias
   */
  def defaultAlias: Alias[Entity]

  /**
   * Returns table name.
   *
   * @return table name
   */
  override def tableName = underlying.tableName

  /**
   * Returns column names.
   *
   * @return column names
   */
  override def columnNames = underlying.columnNames

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
   * Predicates the field name is valid.
   *
   * @param name field name
   * @return valid if true
   */
  def isValidFieldName(name: String): Boolean = {
    try Option(this.column.field(name)).isDefined
    catch { case e: InvalidColumnNameException => false }
  }

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
