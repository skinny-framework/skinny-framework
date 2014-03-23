package unit

import scalikejdbc.{ LoggingSQLAndTimeSettings, GlobalSettings }

trait DBSettings {

  skinny.DBSettings.initialize()
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  lib.DBInitializer.synchronized { lib.DBInitializer.initialize() }

}
