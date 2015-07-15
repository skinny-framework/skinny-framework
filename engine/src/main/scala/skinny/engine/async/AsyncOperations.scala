package skinny.engine.async

import javax.servlet.http.HttpServletRequest

import skinny.engine.context.SkinnyEngineContext

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future, ExecutionContext }

trait AsyncOperations {

  /**
   * Creates a future with implicit request.
   *
   * @param op operation inside this future
   * @param ec execution context
   * @param ctx skinny engine context
   * @tparam A response type
   * @return response value
   */
  @deprecated("Use futureWithContext instead", since = "2.0.0")
  def futureWithRequest[A](op: (HttpServletRequest) => A)(
    implicit ec: ExecutionContext, ctx: SkinnyEngineContext): Future[A] = {
    Future { op(ctx.request) }
  }

  /**
   *  Creates a future with implicit context.
   *
   * @param op operation inside this future
   * @param ec execution context
   * @param context context
   * @tparam A response type
   * @return response value
   */
  def futureWithContext[A](op: (SkinnyEngineContext) => A)(
    implicit ec: ExecutionContext, context: SkinnyEngineContext): Future[A] = {
    Future { op(context) }
  }

  /**
   * Awaits multiple Future's results.
   *
   * @param duration duration to await futures
   * @param fs futures
   * @param ec execution context
   * @return results
   */
  def awaitFutures[A](duration: Duration)(fs: Future[A]*)(implicit ec: ExecutionContext): Seq[A] = {
    Await.result(Future.sequence(fs), duration)
  }

}
