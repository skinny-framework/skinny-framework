package skinny.task.generator

import java.io.File

import com.typesafe.config.Config
import scalikejdbc.config._

trait PlayConfiguration {

  trait PlayDBs extends DBs with TypesafeConfigReader with TypesafeConfig with NoEnvPrefix

  def defaultPlayConfig: Config = skinny.util.TypesafeConfigReader.load(new File("conf/application.conf"))

  lazy val playDBs = new PlayDBs {
    override val config = defaultPlayConfig
  }

}
