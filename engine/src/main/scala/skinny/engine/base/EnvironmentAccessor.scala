package skinny.engine.base

import skinny.engine._

/**
 * Provides accessors for values related to environment.
 */
trait EnvironmentAccessor { self: ServletContextAccessor =>

  def environment: String = {
    sys.props.get(EnvironmentKey) orElse initParameter(EnvironmentKey) getOrElse "DEVELOPMENT"
  }

  /**
   * A boolean flag representing whether the kernel is in development mode.
   * The default is true if the `environment` begins with "dev", case-insensitive.
   */
  def isDevelopmentMode: Boolean = environment.toUpperCase.startsWith("DEV")

}
