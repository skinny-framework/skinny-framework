package skinny.orm

import scalikejdbc.{ GlobalSettings, SQLFormatterSettings }

trait Formatter {
  GlobalSettings.sqlFormatter = SQLFormatterSettings("skinny.mapper.formatter.HibernateSQLFormatter")
}
