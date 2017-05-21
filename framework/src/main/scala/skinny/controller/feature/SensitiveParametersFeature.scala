package skinny.controller.feature

/**
  * Filtering sensitive parameters.
  *
  * config.filter_parameters in Rails.
  */
trait SensitiveParametersFeature {

  /**
    * Returns registered sensitive parameter names.
    */
  protected def sensitiveParameterNames: Seq[String] = Seq(
    "password",
    "password_confirmation",
    "credit_card",
    "password",
    "passwordConfirmation",
    "creditCard"
  )

}
