package skinny.engine.async

import skinny.engine.SkinnyEngineBase
import scala.concurrent.Future

/**
 * Async operations provider.
 */
trait AsyncSupport
    extends AsyncOperations { self: SkinnyEngineBase =>

  /**
   * true if async supported
   */
  override protected def isAsyncExecutable(result: Any): Boolean = {
    classOf[Future[_]].isAssignableFrom(result.getClass) ||
      classOf[AsyncResult].isAssignableFrom(result.getClass)
  }

}
