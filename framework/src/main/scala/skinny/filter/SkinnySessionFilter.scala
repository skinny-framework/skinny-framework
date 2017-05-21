package skinny.filter

import scala.language.implicitConversions

import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.csrf.CSRFTokenGenerator
import skinny.micro.contrib.CSRFTokenSupport

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
trait SkinnySessionFilter extends SkinnySessionFilterBase with BeforeAfterActionFeature {

  self: FlashFeature with CSRFTokenSupport with LocaleFeature =>

  // --------------------------------------
  // SkinnySession by using Skinny beforeAction/afterAction

  beforeAction()(initializeSkinnySession(context))

  afterAction()(saveCurrentSkinnySession()(context))

  // --------------------------------------
  // override CsrfTokenSupport

  override protected def isForged: Boolean = {
    implicit val ctx: SkinnyContext = context
    if (skinnySession.getAttribute(csrfKey).isEmpty) {
      prepareCsrfToken()
    }
    !request.requestMethod.isSafe &&
    skinnySession.getAttribute(csrfKey) != params.get(csrfKey) &&
    !CSRFTokenSupport.HeaderNames.map(request.headers.get).contains(skinnySession.getAttribute(csrfKey))
  }

  override protected def prepareCsrfToken() = {
    skinnySession(context).getAttributeOrElseUpdate(csrfKey, CSRFTokenGenerator())
  }

}

object SkinnySessionFilter {

  val ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE = SkinnySessionFilterBase.ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE

}
