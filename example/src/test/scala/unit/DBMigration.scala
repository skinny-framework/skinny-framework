package unit

import lib.DBInitializer

trait DBMigration extends DBSettings {
  DBInitializer.synchronized {
    DBInitializer.initialize()
  }
}
