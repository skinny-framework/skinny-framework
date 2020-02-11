package skinny.dbmigration

import org.flywaydb.core.Flyway
import skinny.util.TypesafeConfigReader
import skinny.{ DBSettings, SkinnyEnv }
import scalikejdbc.ConnectionPool

import scala.collection.JavaConverters._
import skinny.exception.DBSettingsException

import scala.util.control.NonFatal

object DBMigration extends DBMigration

/**
  * DB migration tool.
  */
trait DBMigration {

  def migrate(env: String = SkinnyEnv.Development, poolName: String = ConnectionPool.DEFAULT_NAME.name): Int = {
    val skinnyEnv = SkinnyEnv.get()
    try {
      System.setProperty(SkinnyEnv.PropertyKey, env)
      DBSettings.initialize()

      try {
        val pool: ConnectionPool = try {
          ConnectionPool.get(Symbol(poolName)) // TODO: remove this symbol conversion by modifying ScalikeJDBC
        } catch {
          case NonFatal(_) =>
            ConnectionPool.get(poolName)
        }
        val flyway = Flyway
          .configure()
          .dataSource(pool.dataSource)

        val migrationConfigPath = s"db.${poolName}.migration"
        val rootConfig          = TypesafeConfigReader.config(env)
        if (rootConfig.hasPath(migrationConfigPath)) {
          rootConfig.getConfig(migrationConfigPath).entrySet.asScala.foreach(println)
          val locations = rootConfig
            .getConfig(migrationConfigPath)
            .getStringList("locations")
            .asScala
            .map(l => "db.migration." + l.replaceAll("/", "."))
          if (locations.nonEmpty) {
            flyway.locations(locations.toIndexedSeq: _*)
          }
        }
        flyway.load().migrate()
      } catch {
        case e: IllegalStateException =>
          throw new DBSettingsException(s"ConnectionPool named $poolName is not found.", e)
      }
    } finally {
      skinnyEnv.foreach { env =>
        System.setProperty(SkinnyEnv.PropertyKey, env)
      }
      DBSettings.initialize()
    }
  }

  def repair(env: String = SkinnyEnv.Development, poolName: String = ConnectionPool.DEFAULT_NAME.name): Unit = {
    val skinnyEnv = SkinnyEnv.get()
    try {
      System.setProperty(SkinnyEnv.PropertyKey, env)
      DBSettings.initialize()
      try {
        val pool = ConnectionPool.get(poolName)
        Flyway
          .configure()
          .dataSource(pool.dataSource)
          .load()
          .repair()
      } catch {
        case e: IllegalStateException =>
          throw new DBSettingsException(s"ConnectionPool named $poolName is not found.")
      }
    } finally {
      skinnyEnv.foreach { env =>
        System.setProperty(SkinnyEnv.PropertyKey, env)
      }
      DBSettings.initialize()
    }
  }

}
