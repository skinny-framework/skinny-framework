// ------------------------------
// Automated code formatter before compilaion
// Disabled by default because this is confusing for beginners
//scalariformSettings

// ------------------------------
// for ./skinnny console
initialCommands := """
import skinny._
import _root_.controller._, model._
import org.joda.time._
import scalikejdbc._, config._
DBSettings.initialize()
"""
