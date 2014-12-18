package skinny

/**
 * Skinny ORM Database settings initializer.
 *
 * @see https://github.com/seratch/scalikejdbc
 */
object DBSettings extends DBSettingsInitializer

/**
 * Database settings initializer mixin.
 */
trait DBSettings {

  DBSettings.initialize()

}
