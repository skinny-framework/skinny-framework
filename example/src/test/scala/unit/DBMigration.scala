package unit

import dev.DBInitializer

trait DBMigration extends DBSettings {
  DBInitializer.synchronized {
    DBInitializer.initialize()
  }
}
