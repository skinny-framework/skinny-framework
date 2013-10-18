scalariformSettings

initialCommands := """
import _root_.controller._, model._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
dev.DBInitializer.runIfFailed(sql"select 1 from companies limit 1")
"""

