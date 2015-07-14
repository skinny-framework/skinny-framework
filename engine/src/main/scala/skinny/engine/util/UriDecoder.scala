package skinny.engine.util

import skinny.engine.routing.PathPatternParser
import rl.UrlCodingUtils

object UriDecoder {

  def firstStep(uri: String): String = {
    UrlCodingUtils.urlDecode(
      toDecode = UrlCodingUtils.ensureUrlEncoding(uri),
      toSkip = PathPatternParser.PathReservedCharacters)
  }

  def secondStep(uri: String): String = {
    uri.replaceAll("%23", "#")
      .replaceAll("%2F", "/")
      .replaceAll("%3F", "?")
  }

}
