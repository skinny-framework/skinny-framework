import scala.language.postfixOps

Compile / env := Some(file("example") / "jetty-env.xml" asFile)
initialCommands := """
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
implicit val session = AutoSession
"""
