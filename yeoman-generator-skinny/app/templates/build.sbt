// Disabled by default because this is confusing for beginners
//scalariformSettings

initialCommands := """
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
"""

