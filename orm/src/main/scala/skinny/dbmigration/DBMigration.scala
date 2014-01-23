package skinny.dbmigration

import com.googlecode.flyway.core.Flyway
import skinny.{ SkinnyEnv, DBSettings }
import scalikejdbc.ConnectionPool
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import skinny.exception.DBSettingsException

object DBMigration extends DBMigration

/**
 * DB migration tool.
 */
trait DBMigration {

  def migrate(env: String = SkinnyEnv.Development, poolName: String = ConnectionPool.DEFAULT_NAME.name) = {
    val skinnyEnv = SkinnyEnv.get()
    try {
      System.setProperty(SkinnyEnv.PropertyKey, env)
      DBSettings.initialize()

      val cp = try {
        Option(ConnectionPool.get(Symbol(poolName)))
      } catch { case e: IllegalStateException => None }

      cp match {
        case Some(pool) => {
          val flyway = new Flyway
          flyway.setDataSource(pool.dataSource)

          val migrationConfigPath = s"${env}.db.${poolName}.migration"
          val rootConfig = ConfigFactory.load()
          if (rootConfig.hasPath(migrationConfigPath)) {
            rootConfig.getConfig(migrationConfigPath).entrySet.asScala.foreach(println)
            val locations = rootConfig.getConfig(migrationConfigPath)
              .getStringList("locations").asScala.map(l => "db.migration." + l.replaceAll("/", "."))
            if (!locations.isEmpty) {
              flyway.setLocations(locations: _*)
            }
          }
          flyway.migrate()
        }
        case None => {
          throw new DBSettingsException(s"ConnectionPool named $poolName is not found.")
        }
      }

    } finally {
      skinnyEnv.foreach { env => System.setProperty(SkinnyEnv.PropertyKey, env) }
      DBSettings.initialize()
    }
  }

}

