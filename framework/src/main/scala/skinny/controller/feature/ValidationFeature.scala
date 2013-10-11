package skinny.controller.feature

import skinny.validator._
import skinny.validator.NewValidation
import skinny.validator.MapValidator
import skinny.exception.RequestScopeConflictException
import org.scalatra.ScalatraBase
import skinny.I18n
import java.util.Locale

trait ValidationFeature extends ScalatraBase
    with RequestScopeFeature
    with SessionLocaleFeature {

  def validation(validations: NewValidation*)(implicit locale: Locale = currentLocale.orNull[Locale]): MapValidator = {
    val prefix: String = null
    validationWithPrefix(prefix, validations: _*)
  }

  def validationWithPrefix(prefix: String, validations: NewValidation*)(implicit locale: Locale = currentLocale.orNull[Locale]): MapValidator = {
    if (params == null) {
      throw new RequestScopeConflictException("You cannot call #validate when Scalatra's #params is empty.")
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

    def withPrefix(key: String) = if (prefix != null) s"${prefix}.${key}" else key

    validator
      .success { _ => }
      .failure { (inputs, errors) =>

        val skinnyValidationMessages = Messages.loadFromConfig(locale = Option(locale))
        val i18n = I18n(locale)

        requestScope("errorMessages", inputs.keys.flatMap { key =>
          errors.get(key).map { error =>
            skinnyValidationMessages.get(
              key = error.name,
              params = i18n.get(withPrefix(key)).getOrElse(key) :: error.messageParams.toList
            ).getOrElse(error.name)
          }
        }.reverse)
        requestScope("keyAndErrorMessages", inputs.keys.map { key =>
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
