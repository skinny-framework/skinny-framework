package skinny.engine.routing

/**
 * Parses a string into a path pattern for routing.
 */
trait PathPatternParser {

  def apply(pattern: String): PathPattern

}

object PathPatternParser {

  val PathReservedCharacters = "/?#"

}
