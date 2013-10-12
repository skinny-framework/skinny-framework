package skinny.orm.formatter

import scalikejdbc.SQLFormatter

/**
 * Hibernate SQL formatter.
 *
 * If you'd like to use this formatter, add hibernate-core library to your dependencies explicitly.
 */
class HibernateSQLFormatter extends SQLFormatter {

  private[this] val formatter = new org.hibernate.engine.jdbc.internal.BasicFormatterImpl()

  override def format(sql: String) = formatter.format(sql)

}
