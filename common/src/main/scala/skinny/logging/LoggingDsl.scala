package skinny.logging

trait LoggingDsl { self: Logging =>

  /**
   * Determine whether trace logging is enabled.
   */
  protected def isTraceEnabled = logger.isTraceEnabled

  /**
   * Issue a trace logging message.
   */
  protected def trace(msg: => Any): Unit = logger.trace(msg)

  /**
   * Issue a trace logging message, with an exception.
   */
  protected def trace(msg: => Any, t: => Throwable): Unit = logger.trace(msg, t)

  /**
   * Determine whether debug logging is enabled.
   */
  protected def isDebugEnabled = logger.isDebugEnabled

  /**
   * Issue a debug logging message.
   */
  protected def debug(msg: => Any): Unit = logger.debug(msg)

  /**
   * Issue a debug logging message, with an exception.
   */
  protected def debug(msg: => Any, t: => Throwable): Unit = logger.debug(msg, t)

  /**
   * Determine whether trace logging is enabled.
   */
  protected def isErrorEnabled = logger.isErrorEnabled

  /**
   * Issue a trace logging message.
   */
  protected def error(msg: => Any): Unit = logger.error(msg)

  /**
   * Issue a trace logging message, with an exception.
   */
  protected def error(msg: => Any, t: => Throwable): Unit = logger.error(msg, t)

  /**
   * Determine whether trace logging is enabled.
   */
  protected def isInfoEnabled = logger.isInfoEnabled

  /**
   * Issue a trace logging message.
   */
  protected def info(msg: => Any): Unit = logger.info(msg)

  /**
   * Issue a trace logging message, with an exception.
   */
  protected def info(msg: => Any, t: => Throwable): Unit = logger.info(msg, t)

  /**
   * Determine whether trace logging is enabled.
   */
  protected def isWarnEnabled = logger.isWarnEnabled

  /**
   * Issue a trace logging message.
   */
  protected def warn(msg: => Any): Unit = logger.warn(msg)

  /**
   * Issue a trace logging message, with an exception.
   */
  protected def warn(msg: => Any, t: => Throwable): Unit = logger.warn(msg, t)

}
