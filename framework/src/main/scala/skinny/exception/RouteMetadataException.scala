package skinny.exception

/**
 * Represents that route's metadata is invalid.
 *
 * @param message message
 * @param cause cause
 */
case class RouteMetadataException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
