package unit

import dev.DBInitializer
import scalikejdbc._, SQLInterpolation._

trait DBMigration extends DBSettings {
  DBInitializer.runIfFailed(sql"select 1 from companies limit 1")
}
