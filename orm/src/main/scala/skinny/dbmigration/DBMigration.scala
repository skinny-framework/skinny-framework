package skinny.dbmigration

import com.googlecode.flyway.core.Flyway
import skinny.{ SkinnyEnv, DBSettings }
import scalikejdbc.ConnectionPool
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

object DBMigration extends DBMigration

/**
 * DB migration tool.
 */
trait DBMigration {

  def migrate(env: String = SkinnyEnv.Development, poolName: String = ConnectionPool.DEFAULT_NAME.name) = {
    val skinnyEnv = SkinnyEnv.get()
    try {
      System.setProperty(SkinnyEnv.Key, env)
      DBSettings.initialize()

      val flyway = new Flyway
      flyway.setDataSource(ConnectionPool.get(Symbol(poolName)).dataSource)

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

    } finally {
      skinnyEnv.foreach { env => System.setProperty(SkinnyEnv.Key, env) }
      DBSettings.initialize()
    }
  }

}

