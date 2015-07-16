package skinny.engine.routing

import javax.servlet.http.HttpServletRequest

import skinny.engine._
import skinny.engine.base.{ SkinnyEngineContextInitializer, ServletContextAccessor, RouteRegistryAccessor }
import skinny.engine.constant._
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.control.HaltPassControl
import skinny.engine.implicits.ServletApiImplicits

/**
 * The core SkinnyEngine DSL.
 */
trait CoreRoutingDsl
    extends HaltPassControl
    with RouteRegistryAccessor
    with SkinnyEngineContextInitializer
    with ServletContextAccessor
    with ServletApiImplicits {

  /**
   * The base path for URL generation
   */
  protected def routeBasePath(implicit ctx: SkinnyEngineContext): String

  /**
   * The SkinnyEngine DSL core methods take a list of [[skinny.engine.routing.RouteMatcher]]
   * and a block as the action body.  The return value of the block is
   * rendered through the pipeline and sent to the client as the response body.
   *
   * See [[SkinnyEngineBase#renderResponseBody]] for the detailed
   * behaviour and how to handle your response body more explicitly, and see
   * how different return types are handled.
   *
   * The block is executed in the context of a CoreDsl instance, so all the
   * methods defined in this trait are also available inside the block.
   *
   * {{{
   *   get("/") {
   *     <form action="/echo">
   *       <label>Enter your name</label>
   *       <input type="text" name="name"/>
   *     </form>
   *   }
   *
   *   post("/echo") {
   *     "hello {params('name)}!"
   *   }
   * }}}
   *
   * SkinnyEngineKernel provides implicit transformation from boolean blocks,
   * strings and regular expressions to [[skinny.engine.RouteMatcher]], so
   * you can write code naturally.
   * {{{
   *   get("/", request.getRemoteHost == "127.0.0.1") { "Hello localhost!" }
   * }}}
   *
   */
  def get(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Get, transformers, action)

  def post(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Post, transformers, action)

  def put(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Put, transformers, action)

  def delete(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Delete, transformers, action)

  def options(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Options, transformers, action)

  def head(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Head, transformers, action)

  def patch(transformers: RouteTransformer*)(action: => Any): Route = addRoute(Patch, transformers, action)

  /**
   * Prepends a new route for the given HTTP method.
   *
   * Can be overriden so that subtraits can use their own logic.
   * Possible examples:
   * $ - restricting protocols
   * $ - namespace routes based on class name
   * $ - raising errors on overlapping entries.
   *
   * This is the method invoked by get(), post() etc.
   *
   * @see skinny.engine.SkinnyEngineKernel#removeRoute
   */
  protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val route: Route = {
      val r = Route(transformers, () => action, (req: HttpServletRequest) => routeBasePath(SkinnyEngineContext.buildWithoutResponse(req, servletContext)))
      r.copy(metadata = r.metadata.updated(Handler.RouteMetadataHttpMethodCacheKey, method))
    }
    routes.prependRoute(method, route)
    route
  }

  private[this] def addStatusRoute(codes: Range, action: => Any): Unit = {
    val route = Route(Seq.empty, () => action, (req: HttpServletRequest) => routeBasePath(skinnyEngineContext(servletContext)))
    routes.addStatusRoute(codes, route)
  }

  /**
   * Defines a block to run if no matching routes are found, or if all
   * matching routes pass.
   */
  def notFound(block: => Any): Unit

  /**
   * Defines a block to run if matching routes are found only for other
   * methods.  The set of matching methods is passed to the block.
   */
  def methodNotAllowed(block: Set[HttpMethod] => Any): Unit

  /**
   * Defines an error handler for exceptions thrown in either the before
   * block or a route action.
   *
   * If the error handler does not match, the result falls through to the
   * previously defined error handler.  The default error handler simply
   * rethrows the exception.
   *
   * The error handler is run before the after filters, and the result is
   * rendered like a standard response.  It is the error handler's
   * responsibility to set any appropriate status code.
   */
  def error(handler: ErrorHandler): Unit

  /**
   * Error handler for HTTP response status code range. You can intercept every response code previously
   * specified with #status or even generic 404 error.
   * {{{
   *   trap(403) {
   *    "You are not authorized"
   *   }
   * }* }}}
   * }}
   */
  def trap(codes: Range)(block: => Any): Unit = {
    addStatusRoute(codes, block)
  }

  /**
   * @see error
   */
  def trap(code: Int)(block: => Any): Unit = {
    trap(Range(code, code + 1))(block)
  }

}
