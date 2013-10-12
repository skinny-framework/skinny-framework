package unit

import tool.DBInitializer

trait DBMigration extends DBSettings {
  DBInitializer.synchronized {
    DBInitializer.initialize()
  }
}
