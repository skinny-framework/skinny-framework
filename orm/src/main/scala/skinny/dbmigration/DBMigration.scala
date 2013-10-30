package skinny.dbmigration

import com.googlecode.flyway.core.Flyway
import skinny.{ SkinnyEnv, DBSettings }
import scalikejdbc.ConnectionPool

object DBMigration extends DBMigration

/**
 * DB migration tool.
 */
trait DBMigration {

  def migrate(env: String) = {
    val skinnyEnv = SkinnyEnv.get()
    try {
      System.setProperty(SkinnyEnv.Key, env)
      DBSettings.initialize()

      val flyway = new Flyway
      flyway.setDataSource(ConnectionPool.dataSource())
      flyway.migrate()

    } finally {
      skinnyEnv.foreach { env => System.setProperty(SkinnyEnv.Key, env) }
      DBSettings.initialize()
    }
  }

}

