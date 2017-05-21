package skinny.filter

import scala.language.implicitConversions

import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.csrf.CSRFTokenGenerator

import skinny.micro.contrib.{ AsyncCSRFTokenSupport, CSRFTokenSupport }
import skinny.controller.feature._

/**
  * Enables replacing Servlet session with Skinny's session shared among several Servlet apps.
  *
  * Mounting skinny.session.SkinnySessionInitializer on the top of Bootstrap.scala is required.
  *
  * {{{
  *   ctx.mount(classOf[SkinnySessionInitializer], "/\*")
  * }}}
  */
trait AsyncSkinnySessionFilter extends SkinnySessionFilterBase with AsyncBeforeAfterActionFeature {

  self: FlashFeature with AsyncCSRFTokenSupport with LocaleFeature =>

  // --------------------------------------
  // SkinnySession by using Skinny beforeAction/afterAction

  beforeAction()(implicit ctx => initializeSkinnySession)

  afterAction()(implicit ctx => saveCurrentSkinnySession)

  // --------------------------------------
  // override CsrfTokenSupport

  override protected def isForged(implicit ctx: SkinnyContext): Boolean = {
    if (skinnySession(context).getAttribute(csrfKey).isEmpty) {
      prepareCsrfToken()
    }
    !request.requestMethod.isSafe &&
    skinnySession.getAttribute(csrfKey) != params.get(csrfKey) &&
    !CSRFTokenSupport.HeaderNames.map(request.headers.get).contains(skinnySession.getAttribute(csrfKey))
  }

  override protected def prepareCsrfToken()(implicit ctx: SkinnyContext) = {
    skinnySession.getAttributeOrElseUpdate(csrfKey, CSRFTokenGenerator())
  }

}
