package skinny.filter

import scala.language.implicitConversions

import skinny.micro.contrib.FlashMapSupport
import skinny.micro.contrib.flash.FlashMap
import skinny.micro.context.SkinnyContext

import skinny.controller.feature._
import skinny.session._
import FlashMapSupport._

import java.util.Locale

/**
  * Enables replacing Servlet session with Skinny's session shared among several Servlet apps.
  *
  * Mounting skinny.session.SkinnySessionInitializer on the top of Bootstrap.scala is required.
  *
  * {{{
  *   ctx.mount(classOf[SkinnySessionInitializer], "/\*")
  * }}}
  */
private[skinny] trait SkinnySessionFilterBase extends SkinnyFilter { self: FlashFeature with LocaleFeature =>

  import SkinnySessionFilterBase._

  /**
    * Replace this when you use other backend.
    */
  protected def initializeSkinnySession(implicit ctx: SkinnyContext): SkinnyHttpSession = {
    // SkinnyHttpSession's factory method doesn't support several backend implementation.
    // Of course, pull requests are always welcome.
    SkinnyHttpSession.getOrCreate(request)
  }

  protected def saveCurrentSkinnySession()(implicit ctx: SkinnyContext): Unit = {
    try {
      getFromRequestScope[SkinnyHttpSession](ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE).foreach { sessionWrapper =>
        sessionWrapper.save()
      }
    } catch {
      case scala.util.control.NonFatal(e) =>
        logger.warn(s"Failed to save skinny session because ${e.getMessage}", e)
    }
  }

  // --------------------------------------
  // Accessing SkinnySession

  def skinnySession(implicit ctx: SkinnyContext): SkinnyHttpSession = {
    getFromRequestScope[SkinnyHttpSession](ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE)(ctx).getOrElse {
      initializeSkinnySession
    }
  }

  def skinnySession[A](key: String)(implicit ctx: SkinnyContext): Option[A] = skinnySession(ctx).getAs[A](key)

  @deprecated(message = "Use skinnySession(String) instead.", since = "4.0.0")
  def skinnySession[A](key: Symbol)(implicit ctx: SkinnyContext): Option[A] = skinnySession[A](key.name)(ctx)

  // --------------------------------------
  // override FlashMapSupport
  // NOTICE: This API doesn't support Future ops

  override def flashMapSetSession(f: FlashMap)(implicit ctx: SkinnyContext): Unit = {
    try {
      skinnySession(ctx).setAttribute(SessionKey, f)
    } catch {
      case scala.util.control.NonFatal(e) =>
        logger.debug(s"Failed to set flashMap to skinny session because ${e.getMessage}")
    }
  }

  // --------------------------------------
  // override SessionLocaleFeature

  override def setCurrentLocale(locale: String)(implicit ctx: SkinnyContext): Unit = {
    skinnySession(ctx).setAttribute(sessionLocaleKey, locale)
  }

  override def currentLocale(implicit ctx: SkinnyContext): Option[Locale] = {
    skinnySession(ctx)
      .getAttribute(sessionLocaleKey)
      .map(l => new Locale(l.toString))
      .orElse(defaultLocale)
  }

}

object SkinnySessionFilterBase {

  val ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE = classOf[SkinnySessionFilter].getCanonicalName + "_SkinnySessionWrapper"

}
