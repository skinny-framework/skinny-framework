package skinny.controller.feature

import skinny.validator._
import skinny.validator.NewValidation
import skinny.validator.MapValidator
import org.scalatra.ScalatraBase
import skinny.I18n
import java.util.Locale

/**
 * Validation support for Skinny app.
 */
trait ValidationFeature {

  self: ScalatraBase with RequestScopeFeature with SessionLocaleFeature =>

  /**
   * Creates new validation form.
   *
   * @param validations validations
   * @param locale current locale
   * @return validator
   */
  def validation(validations: NewValidation*)(implicit locale: Locale = currentLocale.orNull[Locale]): MapValidator = {
    validationWithPrefix(null, validations: _*)
  }

  /**
   * Creates new validation form.
   *
   * @param prefix key prefix for error message
   * @param validations validations
   * @param locale current locale
   * @return validator
   */
  def validationWithPrefix(prefix: String, validations: NewValidation*)(
    implicit locale: Locale = currentLocale.orNull[Locale]): MapValidator = {

    if (params == null) {
      throw new IllegalStateException("You cannot call #validation when Scalatra's #params is absent.")
    }

    val validator = new MapValidator(params, Validations(params, validations)) {
      override def validate(): Boolean = {
        if (hasErrors) {
          status = 400
          setParams()
        }
        super.validate()
      }
    }

    validator
      .success { _ => }
      .failure { (inputs, errors) =>
        val skinnyValidationMessages = Messages.loadFromConfig(locale = Option(locale))
        val i18n = I18n(locale)
        def withPrefix(key: String) = if (prefix != null) s"${prefix}.${key}" else key

        set(RequestScopeFeature.ATTR_ERROR_MESSAGES, inputs.keys.flatMap { key =>
          errors.get(key).map { error =>
            skinnyValidationMessages.get(
              key = error.name,
              params = i18n.get(withPrefix(key)).getOrElse(key) :: error.messageParams.toList
            ).getOrElse(error.name)
          }
        }.reverse)

        set(RequestScopeFeature.ATTR_KEY_AND_ERROR_MESSAGES, inputs.keys.map { key =>
          key -> errors.get(key).map { error =>
            skinnyValidationMessages.get(
              key = error.name,
              params = i18n.get(withPrefix(key)).getOrElse(key) :: error.messageParams.toList
            ).getOrElse(error.name)
          }
        }.toMap)
      }.apply()

    validator
  }

}
