package skinny.engine.base

import javax.servlet.ServletContext

import skinny.engine.context.SkinnyEngineContext

/**
 * When this trait is activated, thread-local request/response needed by SkinnyEngineContext are always accessible.
 */
trait MainThreadLocalEverywhere
    extends SkinnyEngineContextInitializer {

  self: ServletContextAccessor with UnstableAccessValidationConfig =>

  /**
   * Skinny Engine Context
   */
  override implicit def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    super.skinnyEngineContext(ctx)
  }

}
