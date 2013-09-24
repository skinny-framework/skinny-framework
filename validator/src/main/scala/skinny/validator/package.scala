package skinny

import scala.language.implicitConversions

package object validator {

  def param(kv: (String, Any)): KeyValueParamDefinition = KeyValueParamDefinition(kv._1, kv._2)

  def paramKey(name: String): KeyParamDefinition = KeyParamDefinition(name)

  def checkAll(vs: ValidationRule*): ValidationRule = {
    def merge(v1: ValidationRule, v2: ValidationRule): ValidationRule = {
      new Object with ValidationRule {

        def name: String = "combined-results"
        def isValid(value: Any): Boolean = throw new IllegalStateException

        override def apply(param: KeyValueParamDefinition): Validation = {
          v1.apply(param) match {
            case res1: ValidationSuccess => v2.apply(param)
            case res1: ValidationFailure =>
              ValidationFailure(param = param, errors = res1.errors ++ v2.apply(param).errors)
            case _ => throw new IllegalStateException
          }
        }

      }
    }
    vs.tail.foldLeft(vs.head) { case (vs, v) => merge(vs, v) }
  }

  private[validator] class ParamDefinitionWithIs(param: ParamDefinition) {

    def is(validations: ValidationRule): NewValidation = NewValidation(param, validations)

    def are(validations: ValidationRule): NewValidation = NewValidation(param, validations)

  }

  implicit def convertParamDefinitionToParamDefinitionWithIs(param: ParamDefinition): ParamDefinitionWithIs = {
    new ParamDefinitionWithIs(param)
  }

}

