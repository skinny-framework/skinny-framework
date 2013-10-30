package skinny.bootstrap

import scalikejdbc._

/**
 * Skinny ORM Database settings initializer.
 *
 * @see https://github.com/seratch/scalikejdbc
 */
object DBSettings {

  def initialize(): Unit = {
    // logging SQL & time
    GlobalSettings.loggingSQLAndTime = new LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = true,
      logLevel = 'DEBUG
    )
    skinny.DBSettings.initialize()
  }

}
