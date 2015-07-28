package skinny.engine.base

import javax.servlet.ServletContext

import skinny.engine.context.SkinnyEngineContext

trait MainThreadLocalEverywhere extends SkinnyEngineContextInitializer {

  self: ServletContextAccessor with UnstableAccessValidationConfig =>

  /**
   * Skinny Engine Context
   */
  override implicit def skinnyEngineContext(implicit ctx: ServletContext): SkinnyEngineContext = {
    super.skinnyEngineContext(ctx)
  }

}
