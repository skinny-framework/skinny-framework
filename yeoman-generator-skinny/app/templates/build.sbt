scalariformSettings

initialCommands := """
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, SQLInterpolation._, config._
DBsWithEnv("development").setupAll()
"""

seq(scalateSettings:_*)

// Scalate Precompilation and Bindings
scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
  Seq( TemplateConfig(file(".") / "src" / "main" / "webapp" / "WEB-INF",  Nil,  Nil,  Some("templates")))
}
