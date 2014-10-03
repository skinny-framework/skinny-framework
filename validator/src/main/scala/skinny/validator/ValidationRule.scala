package skinny.validator

import scala.language.reflectiveCalls

/**
 * Validation rule.
 */
trait ValidationRule extends ((KeyValueParamDefinition) => ValidationState) with ErrorLike {

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
    else ValidationFailure(paramDef, Seq(Error(this.name, this.messageParams)))
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

  protected def isNumeric(v: Any): Boolean = {
    !isEmpty(v) && """\A((-|\+)?[0-9]+(\.[0-9]+)?)+\z""".r.findFirstIn(v.toString).isDefined
  }

  protected def toDouble(v: Any): Double = v.toString.toDouble

  protected def isDouble(v: Any): Boolean = isEmpty(v) || {
    try {
      toDouble(v)
      true
    } catch {
      case e: NullPointerException => false
      case e: NumberFormatException => false
    }
  }

  protected def toFloat(v: Any): Float = v.toString.toFloat

  protected def isFloat(v: Any): Boolean = isEmpty(v) || {
    try {
      toFloat(v)
      true
    } catch {
      case e: NullPointerException => false
      case e: NumberFormatException => false
    }
  }

  protected def toInt(v: Any): Int = v.toString.toInt

  protected def isInt(v: Any): Boolean = isEmpty(v) || {
    try {
      toInt(v)
      true
    } catch {
      case e: NullPointerException => false
      case e: NumberFormatException => false
    }
  }

  protected def toLong(v: Any): Long = v.toString.toLong

  protected def isLong(v: Any): Boolean = isEmpty(v) || {
    try {
      toLong(v)
      true
    } catch {
      case e: NullPointerException => false
      case e: NumberFormatException => false
    }
  }

  protected def toHasSize(v: Any): Option[{ def size(): Int }] = {
    val x = v.asInstanceOf[{ def size(): Int }]
    try {
      x.size
      Option(x)
    } catch { case e: NoSuchMethodException => None }
  }

  protected def toHasGetTime(v: Any): Option[{ def getTime(): Long }] = {
    try {
      Option(v.asInstanceOf[{ def toDate(): java.util.Date }].toDate)
    } catch {
      case e: NoSuchMethodException =>
        val x = v.asInstanceOf[{ def getTime(): Long }]
        try {
          x.getTime
          Option(x)
        } catch { case e: NoSuchMethodException => None }
    }
  }

  protected def nowMillis(): Long = System.currentTimeMillis

}

