/*
  ---------------------------------------------------------------------------
  This software is released under a BSD license, adapted from
  http://opensource.org/licenses/bsd-license.php

  Copyright (c) 2010, Brian M. Clapper
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

   * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

   * Neither the names "clapper.org", "AVSL", nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ---------------------------------------------------------------------------
*/
package skinny.logging

/**
 * Mix the `Logging` trait into a class to get:
 *
 * - Logging methods
 * - A `Logger` object, accessible via the `log` property
 *
 * Does not affect the public API of the class mixing it in.
 */
trait Logging {

  // The logger. Instantiated the first time it's used.
  private lazy val _logger = Logger(getClass)

  /**
   * Get the `Logger` for the class that mixes this trait in. The `Logger`
   * is created the first time this method is call. The other methods (e.g.,
   * `error`, `info`, etc.) call this method to get the logger.
   */
  protected def logger: Logger = _logger

  /**
   * Get the name associated with this logger.
   */
  protected def loggerName = logger.name

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

