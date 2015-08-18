package skinny

import scala.language.implicitConversions

/**
 * Skinny validator provides easy-to-understand and readable DSLs to validate inputs.
 */
package object validator {

  /**
   * Accepts key and value.
   *
   * @param kv key and value
   * @return param definition
   */
  def param(kv: (String, Any)): KeyValueParamDefinition = KeyValueParamDefinition(kv._1, kv._2)

  /**
   * Accepts key.
   *
   * @param name key
   * @return param definition
   */
  def paramKey(name: String): OnlyKeyParamDefinition = OnlyKeyParamDefinition(name)

  /**
   * Converts validation rules to a combined validation rule which verify all the rules even if some of them has errors.
   *
   * @param validationRules validation rules
   * @return validation rule
   */
  def checkAll(validationRules: ValidationRule*): ValidationRule = {
    def merge(v1: ValidationRule, v2: ValidationRule): ValidationRule = {
      new Object with ValidationRule {

        def name: String = "combined-results"
        def isValid(value: Any): Boolean = throw new IllegalStateException

        override def apply(paramDef: KeyValueParamDefinition): ValidationState = {
          v1.apply(paramDef) match {
            case res1: ValidationSuccess => v2.apply(paramDef)
            case res1: ValidationFailure =>
              ValidationFailure(paramDef = paramDef, errors = res1.errors ++ v2.apply(paramDef).errors)
            case _ => throw new IllegalStateException
          }
        }

      }
    }
    validationRules.tail.foldLeft(validationRules.head) { case (vs, v) => merge(vs, v) }
  }

  /**
   * Param definition which has #is and #are DSL methods.
   *
   * @param paramDef param definition
   */
  private[validator] class ParamDefinitionWithIsDSL(paramDef: ParamDefinition) {

    def is(validations: ValidationRule): NewValidation = NewValidation(paramDef, validations)

    def are(validations: ValidationRule): NewValidation = NewValidation(paramDef, validations)

  }

  /**
   * Converts ParamDefinition to ParamDefinitionWithIsDSL implicitly.
   *
   * @param paramDef param definition
   * @return with dsl
   */
  implicit def convertParamDefinitionToParamDefinitionWithIs(paramDef: ParamDefinition): ParamDefinitionWithIsDSL = {
    new ParamDefinitionWithIsDSL(paramDef)
  }

}
