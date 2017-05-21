package skinny.exception

/**
  * assets:precompile task failure.
  *
  * @param message message
  * @param cause cause
  */
case class AssetsPrecompileFailureException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)
