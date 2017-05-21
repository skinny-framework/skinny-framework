package unit

import scalikejdbc.{ GlobalSettings, LoggingSQLAndTimeSettings }

trait DBSettings {

  skinny.DBSettings.initialize()
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  lib.DBInitializer.synchronized { lib.DBInitializer.initialize() }

}
