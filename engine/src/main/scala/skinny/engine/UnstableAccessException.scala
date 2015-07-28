package skinny.engine

/**
 * Represents unstable access to servlet objects managed by containers from unmanaged threads.
 */
class UnstableAccessException(attribute: String)
    extends RuntimeException(UnstableAccessException.message(attribute)) {

}

object UnstableAccessException {

  def message(attribute: String): String = {
    s"""
      |
      |------------------------------------------------------
      |
      |  !!! Concurrency Issue Detected !!!
      |
      |  Accessing $attribute from unmanaged threads, inside Future blocks in most cases, is too dangerous.
      |
      |  Objects managed by Servlet containers should be accessed on main threads.
      |
      |  Fix your code to copy needed values from $attribute as read-only ones before entering Future blocks.
      |
      |  Or, if you accept the risk, set the web controller's #unstableAccessValidationEnabled as false (default: true).
      |
      |------------------------------------------------------
      |""".stripMargin
  }

}
