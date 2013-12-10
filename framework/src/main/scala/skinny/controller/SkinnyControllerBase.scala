package skinny.controller

import skinny.controller.feature._
import skinny.validator.implicits.ParametersGetAsImplicits
import skinny.controller.implicits.ParamsPermitImplicits
import skinny.routing.implicits.RoutesAsImplicits
import org.scalatra.HttpMethod
import skinny.{ I18n, Format }
import java.util.Locale

trait SkinnyControllerBase
    extends org.scalatra.ScalatraBase
    with EnvFeature
    with RichRouteFeature
    with ExplicitRedirectFeature
    with RequestScopeFeature
    with ActionDefinitionFeature
    with BeforeAfterActionFeature
    with SessionLocaleFeature
    with FlashFeature
    with ValidationFeature
    with JSONFeature
    with TemplateEngineFeature
    with ScalateTemplateEngineFeature
    with CSRFProtectionFeature
    with RoutesAsImplicits
    with ParametersGetAsImplicits
    with ParamsPermitImplicits
    with grizzled.slf4j.Logging {

  /**
   * Defines formats to be respond. By default, HTML, JSON, XML are available.
   *
   * @return formats
   */
  protected def respondTo: Seq[Format] = Seq(Format.HTML, Format.JSON, Format.XML)

  /**
   * Provides code block with format. If absent, halt as status 406.
   *
   * @param format format
   * @param action action
   * @tparam A response type
   * @return result
   */
  protected def withFormat[A](format: Format)(action: => A): A = {
    respondTo.find(_ == format) getOrElse haltWithBody(406)
    action
  }

  /**
   * Creates skinny.I18n instance for current locale.
   *
   * @param locale current locale
   * @return i18n provider
   */
  protected def createI18n()(implicit locale: java.util.Locale = currentLocale.orNull[Locale]) = I18n(locale)

}
