package skinny.orm.feature

import skinny.orm._
import scalikejdbc._, SQLInterpolation._

trait BasicFeature[Entity] extends SQLSyntaxSupport[Entity] {

  protected def singleSelectQuery: SelectSQLBuilder[Entity] = select.from(as(defaultAlias))

  def primaryKeyName: String = "id"

  def defaultAlias: Alias[Entity] = syntax

  def createAlias(name: String): Alias[Entity] = syntax(name)

  def withAlias[A](op: Alias[Entity] => A): A = op(defaultAlias)

  def withAlias[A](name: String)(op: Alias[Entity] => A): A = op(createAlias(name))

  def withColumns[A](op: ColumnName[Entity] => A): A = op(column)

  def apply(a: Alias[Entity])(rs: WrappedResultSet): Entity = extract(rs, a.resultName)

  def apply(rs: WrappedResultSet): Entity = extract(rs, defaultAlias.resultName)

  def extract(rs: WrappedResultSet, n: ResultName[Entity]): Entity

}
