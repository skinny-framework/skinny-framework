package skinny.bootstrap

import scalikejdbc._, config._
import skinny.SkinnyEnv
import skinny.exception._

object DBSettings {

  def initialize(): Unit = {
    // load sql formatter
    val className = "skinny.orm.formatter.HibernateSQLFormatter"
    GlobalSettings.sqlFormatter = SQLFormatterSettings(className)
    // load db settings
    SkinnyEnv.get().map(env => DBsWithEnv(env).setupAll()).getOrElse {
      if (!TypesafeConfigReaderWithEnv("development").dbNames.isEmpty) {
        DBsWithEnv("development").setupAll()
      } else if (!TypesafeConfigReader.dbNames.isEmpty) {
        DBs.setupAll()
      } else {
        throw new DBSettingsException(s"""
        | ---------------------------------------------
        |
        |  !!! Skinny Configuration Error !!!
        |
        |  DB settings was not found. Add some db settings to src/main/resources/application.conf like this:
        |
        |   development {
        |     db {
        |       default {
        |         driver="org.h2.Driver"
        |         url="jdbc:h2:mem:example"
        |         user="sa"
        |         password="sa"
        |         poolInitialSize=2
        |         poolMaxSize=10
        |       }
        |     }
        |   }
        |
        |
        |  "development" is the default env value. If you don't need env prefix it also works.
        |  You can pass env value from environment variables "export skinny.env=qa" or via JVM option like "-Dskinny.env=qa".
        |  Though you're not forced to use these values, recommended env values are "development", "test", "qa" and "production".
        |
        | ---------------------------------------------
        |""".stripMargin)
      }
    }
  }

}
