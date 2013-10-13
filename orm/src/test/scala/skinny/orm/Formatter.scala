package skinny.orm

import scalikejdbc.{ SQLFormatterSettings, GlobalSettings }

trait Formatter {
  GlobalSettings.sqlFormatter = SQLFormatterSettings("skinny.mapper.formatter.HibernateSQLFormatter")
}
