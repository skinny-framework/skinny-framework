package skinny.exception

case class RouteMetadataException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
