package skinny.controller.feature

import java.util.Locale

trait SessionLocaleFeature extends org.scalatra.ScalatraBase {

  def sessionLocaleKey = "locale"

  def setCurrentLocale(locale: String) = session.put(sessionLocaleKey, locale)

  def currentLocale: Option[Locale] = session.get(sessionLocaleKey).map(l => new Locale(l.toString))

}

