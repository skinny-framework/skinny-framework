package skinny.bootstrap

import scalikejdbc._, config._
import skinny.SkinnyEnv

object DBSettings {

  def initialize(): Unit = {
    // load sql formatter
    val className = "skinny.orm.formatter.HibernateSQLFormatter"
    GlobalSettings.sqlFormatter = SQLFormatterSettings(className)
    // load db settings
    SkinnyEnv.get().map(env => DBsWithEnv(env).setupAll()).getOrElse(DBs.setupAll())
  }

}
