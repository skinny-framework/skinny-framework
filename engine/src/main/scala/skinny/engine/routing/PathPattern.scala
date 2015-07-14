package skinny.engine.routing

import skinny.engine.MultiParams

import scala.util.matching.Regex

/**
 * A path pattern optionally matches a request path and extracts path
 * parameters.
 */
case class PathPattern(regex: Regex, captureGroupNames: List[String] = Nil) {

  def apply(path: String): Option[MultiParams] = {
    // This is a performance hotspot.  Hideous mutatations ahead.
    val m = regex.pattern.matcher(path)
    var multiParams = Map[String, Seq[String]]()
    if (m.matches) {
      var i = 0
      captureGroupNames foreach { name =>
        i += 1
        val value = m.group(i)
        if (value != null) {
          val values = multiParams.getOrElse(name, Vector()) :+ value
          multiParams = multiParams.updated(name, values)
        }
      }
      Some(multiParams)
    } else None
  }

  def +(pathPattern: PathPattern): PathPattern = PathPattern(
    new Regex(this.regex.toString + pathPattern.regex.toString),
    this.captureGroupNames ::: pathPattern.captureGroupNames
  )

}
