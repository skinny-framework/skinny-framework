// ------------------------------
// for ./skinnny package:standalone
import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("skinny.standalone.JettyLauncher")

test in assembly := {}

// ------------------------------
// Disabled by default because this is confusing for beginners
//scalariformSettings

// ------------------------------
// for ./skinnny console
initialCommands := """
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
"""


