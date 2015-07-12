package skinny.engine.routing

object PathPatternParser {

  val PathReservedCharacters = "/?#"

}

/**
 * Parses a string into a path pattern for routing.
 */
trait PathPatternParser {

  def apply(pattern: String): PathPattern

}