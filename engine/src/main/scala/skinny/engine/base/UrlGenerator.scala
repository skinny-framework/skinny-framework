package skinny.engine.base

import skinny.SkinnyEnv
import skinny.engine.SkinnyEngineBase
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.implicits.RicherStringImplicits
import skinny.engine.routing.CoreRoutingDsl

import scala.util.control.Exception._

trait UrlGenerator extends RicherStringImplicits { self: ServletContextAccessor with CoreRoutingDsl =>

  private[this] def needHttps: Boolean = {
    allCatch.withApply(_ => false) {
      servletContext.getInitParameter(SkinnyEngineBase.ForceHttpsKey).blankOption
        .map(_.toBoolean) getOrElse false
    }
  }

  private[this] def appendSessionIdToUri(uri: String)(implicit ctx: SkinnyEngineContext): String = {
    ctx.response.encodeURL(uri)
  }

  def relativeUrl(
    path: String,
    params: Iterable[(String, Any)] = Iterable.empty,
    includeContextPath: Boolean = true,
    includeServletPath: Boolean = true)(implicit ctx: SkinnyEngineContext): String = {
    url(path, params, includeContextPath, includeServletPath, absolutize = false)(ctx)
  }

  /**
   * Returns a context-relative, session-aware URL for a path and specified parameters.
   * Finally, the result is run through `response.encodeURL` for a session
   * ID, if necessary.
   *
   * @param path the base path.  If a path begins with '/', then the context
   *             path will be prepended to the result
   *
   * @param params params, to be appended in the form of a query string
   *
   * @return the path plus the query string, if any.  The path is run through
   *         `response.encodeURL` to add any necessary session tracking parameters.
   */
  // TODO: 2.0.0 still has this issue. Remove this override when it will be fixed in the future
  def url(
    path: String,
    params: Iterable[(String, Any)] = Iterable.empty,
    includeContextPath: Boolean = true,
    includeServletPath: Boolean = true,
    absolutize: Boolean = true,
    withSessionId: Boolean = true)(implicit ctx: SkinnyEngineContext): String = {
    try {
      val newPath = path match {
        case x if x.startsWith("/") && includeContextPath && includeServletPath =>
          ensureSlash(routeBasePath(ctx)) + ensureContextPathsStripped(ensureSlash(path))(ctx)
        case x if x.startsWith("/") && includeContextPath =>
          ensureSlash(contextPath) + ensureContextPathStripped(ensureSlash(path))
        case x if x.startsWith("/") && includeServletPath => ctx.request.getServletPath.blankOption map {
          ensureSlash(_) + ensureServletPathStripped(ensureSlash(path))(ctx)
        } getOrElse "/"
        case _ if absolutize => ensureContextPathsStripped(ensureSlash(path))(ctx)
        case _ => path
      }

      val pairs = params map {
        case (key, None) => key.urlEncode + "="
        case (key, Some(value)) => key.urlEncode + "=" + value.toString.urlEncode
        case (key, value) => key.urlEncode + "=" + value.toString.urlEncode
      }
      val queryString = if (pairs.isEmpty) "" else pairs.mkString("?", "&", "")
      if (withSessionId) appendSessionIdToUri(newPath + queryString)(ctx) else newPath + queryString
    } catch {
      case e: NullPointerException =>
        // work around for Scalatra issue
        if (SkinnyEnv.isTest()) "[work around] see https://github.com/scalatra/scalatra/issues/368"
        else throw e
    }
  }

  /**
   * Builds a full URL from the given relative path. Takes into account the port configuration, https, ...
   *
   * @param path a relative path
   *
   * @return the full URL
   */
  def fullUrl(
    path: String,
    params: Iterable[(String, Any)] = Iterable.empty,
    includeContextPath: Boolean = true,
    includeServletPath: Boolean = true,
    withSessionId: Boolean = true)(implicit ctx: SkinnyEngineContext): String = {
    if (path.startsWith("http")) path
    else {
      val p = url(path, params, includeContextPath, includeServletPath, withSessionId)(ctx)
      if (p.startsWith("http")) p else buildBaseUrl(ctx) + ensureSlash(p)
    }
  }

  private[this] def buildBaseUrl(implicit ctx: SkinnyEngineContext): String = {
    "%s://%s".format(
      if (needHttps || ctx.request.isHttps) "https" else "http",
      serverAuthority(ctx)
    )
  }

  private[this] def ensureContextPathsStripped(path: String)(implicit ctx: SkinnyEngineContext): String = {
    ((ensureContextPathStripped _) andThen (p => ensureServletPathStripped(p)(ctx)))(path)
  }

  private[this] def ensureServletPathStripped(path: String)(implicit ctx: SkinnyEngineContext): String = {
    val sp = ensureSlash(Option(ctx.request.getServletPath).flatMap(_.blankOption).getOrElse(""))
    val np = if (path.startsWith(sp + "/")) path.substring(sp.length) else path
    ensureSlash(np)
  }

  private[this] def ensureContextPathStripped(path: String): String = {
    val cp = ensureSlash(contextPath)
    val np = if (path.startsWith(cp + "/")) path.substring(cp.length) else path
    ensureSlash(np)
  }

  private[this] def ensureSlash(candidate: String): String = {
    if (candidate == null) {
      ""
    } else {
      val p = if (candidate.startsWith("/")) candidate else "/" + candidate
      if (p.endsWith("/")) p.dropRight(1) else p
    }
  }

}
