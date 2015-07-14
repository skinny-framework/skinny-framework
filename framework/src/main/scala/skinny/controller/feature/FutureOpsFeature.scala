package skinny.controller.feature

import skinny.controller.SkinnyControllerBase
import javax.servlet.http.HttpServletRequest
import skinny.engine.context.SkinnyEngineContext

import scala.concurrent._
import scala.concurrent.duration.Duration

/**
 * Provides seamless Future operations within SkinnyController.
 * This trait enables accessing request, session and RequestScope from Future operations safely.
 *
 * {{{
 *   import scala.concurrent._
 *   import scala.concurrent.duration._
 *   import scala.concurrent.ExecutionContext.Implicits.global
 *
 *   class SomeControllerOps(controller: SomeController) {
 *     def setOther(implicit req: HttpServletRequest) = {
 *       controller.set("bar", Seq(1,2,3)) // implicit request is not ambiguous here
 *     }
 *   }
 *
 *   class SomeController extends ApplicationController {
 *     def index = {
 *       val ops = new SomeControllerOps(this)
 *       awaitFutures(5.seconds)(
 *         futureWithRequest { implicit req =>
 *           // Explicitly specify HttpServletRequest here
 *           set("foo" -> FooService.getSomething()))(req)
 *         },
 *         futureWithRequest(req => ops.setOther(req))
 *       )
 *       render("/some/index")
 *     }
 *   }
 * }}}
 */
trait FutureOpsFeature { self: SkinnyControllerBase =>

  /**
   * Creates a future with implicit request.
   *
   * @param op operation inside this future
   * @param ec execution context
   * @param req request
   * @tparam A response type
   * @return response value
   */
  @deprecated("Use futureWithContext instead", since = "2.0.0")
  def futureWithRequest[A](op: (HttpServletRequest) => A)(
    implicit ec: ExecutionContext, req: HttpServletRequest): Future[A] = {
    Future { op(req) }
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
