package skinny.validator

import scala.language.reflectiveCalls

/**
 * Validation rule.
 */
trait ValidationRule extends ((KeyValueParamDefinition) => ValidationState) with Error {

  /**
   * Validation itself.
   *
   * @param value any value
   * @return valid if true
   */
  def isValid(value: Any): Boolean

  /**
   * Applies this validation rule to parameter.
   *
   * @param paramDef param definition
   * @return validation
   */
  def apply(paramDef: KeyValueParamDefinition): ValidationState = {
    if (isValid(paramDef.value)) ValidationSuccess(paramDef)
    else ValidationFailure(paramDef, Seq(this))
  }

  /**
   * Adds new and condition.
   *
   * @param that new validation rule
   * @return combined validation rule
   */
  def and(that: ValidationRule): ValidationRule = &(that)

  /**
   * Adds new and condition.
   *
   * @param that new validation rule
   * @return combined validation rule
   */
  def &(that: ValidationRule): ValidationRule = {
    val _this = this
    new Object with ValidationRule {

      def name: String = "combined-results"
      def isValid(value: Any): Boolean = throw new IllegalStateException

      override def apply(paramDef: KeyValueParamDefinition): ValidationState = {
        _this.apply(paramDef) match {
          case _: ValidationSuccess => that.apply(paramDef)
          case f: ValidationFailure => f
          case _ => throw new IllegalStateException
        }
      }

    }
  }

  protected def isEmpty(v: Any): Boolean = v == null || v == ""

  protected def toHasSize(v: Any): Option[{ def size(): Int }] = {
    try {
      val x = v.asInstanceOf[{ def size(): Int }]
      x.size
      Option(x)
    } catch { case e: NoSuchMethodException => None }
  }

  protected def toHasGetTime(v: Any): Option[{ def getTime(): Long }] = {
    try {
      Option(v.asInstanceOf[{ def toDate(): java.util.Date }].toDate)
    } catch {
      case e: NoSuchMethodException =>
        try {
          val x = v.asInstanceOf[{ def getTime(): Long }]
          x.getTime
          Option(x)
        } catch { case e: NoSuchMethodException => None }
    }
  }

  protected def nowMillis(): Long = System.currentTimeMillis

}

