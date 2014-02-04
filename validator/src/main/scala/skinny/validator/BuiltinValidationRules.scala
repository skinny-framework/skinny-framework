package skinny.validator

import scala.language.reflectiveCalls
import skinny.util.DateTimeUtil

/**
 * Built-in validation rules.
 */

// ----
// param("x" -> "") is notNull

object notNull extends ValidationRule {
  def name = "notNull"
  def isValid(v: Any) = v != null
}

// ----
// param("x" -> "y") is required

object required extends required(true)

case class required(trim: Boolean = true) extends ValidationRule {
  def name = "required"
  def isValid(v: Any) = !isEmpty(v) && {
    if (trim) v.toString.trim.length > 0
    else v.toString.length > 0
  }
}

// ----
// param("x" -> "y") is notEmpty
// param("list" -> Seq(1,2,3)) is notEmpty

object notEmpty extends notEmpty(true)

case class notEmpty(trim: Boolean = true) extends ValidationRule {
  def name = "notEmpty"
  def isValid(v: Any) = !isEmpty(v) && {
    toHasSize(v).map {
      x => x.size > 0
    }.getOrElse {
      if (trim) v.toString.trim.length > 0
      else v.toString.length > 0
    }
  }
}

case class length(len: Int) extends ValidationRule {
  def name = "length"
  override def messageParams = Seq(len.toString)
  def isValid(v: Any) = isEmpty(v) || {
    toHasSize(v).map {
      x => x.size == len
    }.getOrElse {
      v.toString.length == len
    }
  }
}

// ----
// param("x" -> "yyyymmdd") is minLength(3)
// param("list" -> (1 to 5)) is minLength(3)

case class minLength(min: Int) extends ValidationRule {
  def name = "minLength"
  override def messageParams = Seq(min.toString)
  def isValid(v: Any) = isEmpty(v) || {
    toHasSize(v).map {
      x => x.size >= min
    }.getOrElse {
      v.toString.length >= min
    }
  }
}

// ----
// param("x" -> "y") is maxLength(3)
// param("list" -> Seq(1,2)) is maxLength(3)

case class maxLength(max: Int) extends ValidationRule {
  def name = "maxLength"
  override def messageParams = Seq(max.toString)
  def isValid(v: Any) = isEmpty(v) || {
    toHasSize(v).map {
      x => x.size <= max
    }.getOrElse {
      v.toString.length <= max
    }
  }
}

// ----
// param("x" -> "y") is minMaxLength(3, 6)
// param("list" -> Seq(1,2,3,4)) is minMaxLength(3, 6)

case class minMaxLength(min: Int, max: Int) extends ValidationRule {
  def name = "minMaxLength"
  override def messageParams = Seq(min.toString, max.toString)
  def isValid(v: Any) = isEmpty(v) || {
    toHasSize(v).map {
      x => x.size >= min && x.size <= max
    }.getOrElse {
      v.toString.length >= min && v.toString.length <= max
    }
  }
}

// ----
// param("x" -> "123") is numeric
// param("x" -> 0.123D) is numeric

object numeric extends ValidationRule {
  def name = "numeric"
  def isValid(v: Any) = isEmpty(v) ||
    "^((-|\\+)?[0-9]+(\\.[0-9]+)?)+$".r.findFirstIn(v.toString).isDefined
}

// ----
// param("x" -> "123") is intValue
object intValue extends ValidationRule {
  def name = "intValue"
  def isValid(v: Any) = isEmpty(v) || {
    try {
      v.toString.toInt
      true
    } catch { case e: NumberFormatException => false }
  }
}

// ----
// param("x" -> "123") is longValue

object longValue extends ValidationRule {
  def name = "longValue"
  def isValid(v: Any) = isEmpty(v) || {
    try {
      v.toString.toLong
      true
    } catch { case e: NumberFormatException => false }
  }
}

// ----
// param("x" -> "1.7976931348623157E308") is doubleValue

object doubleValue extends ValidationRule {
  def name = "doubleValue"
  def isValid(v: Any) = isEmpty(v) || {
    try {
      v.toString.toDouble
      true
    } catch { case e: NumberFormatException => false }
  }
}

// ----
// param("x" -> "3.4028235E38") is floatValue

object floatValue extends ValidationRule {
  def name = "floatValue"
  def isValid(v: Any) = isEmpty(v) || {
    try {
      v.toString.toFloat
      true
    } catch { case e: NumberFormatException => false }
  }
}

// ----
// param("x" -> 4) is intMinMaxValue(3, 5)

case class intMinMaxValue(min: Int, max: Int) extends ValidationRule {
  def name = "intMinMaxValue"
  override def messageParams = Seq(min.toString, max.toString)
  def isValid(v: Any) = v == null || v.toString.toInt >= min && v.toString.toInt <= max
}

// ----
// param("x" -> 2) is intMinValue(3)

case class intMinValue(min: Int) extends ValidationRule {
  def name = "intMinValue"
  override def messageParams = Seq(min.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toInt >= min
}

// ----
// param("x" -> 4) is intMaxValue(5)

case class intMaxValue(max: Int) extends ValidationRule {
  def name = "intMaxValue"
  override def messageParams = Seq(max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toInt <= max
}

// ----
// param("x" -> "3") is longMinMaxValue(3L, 5L)

case class longMinMaxValue(min: Long, max: Long) extends ValidationRule {
  def name = "longMinMaxValue"
  override def messageParams = Seq(min.toString, max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toLong >= min && v.toString.toLong <= max
}

// ----
// param("x" -> 5) is longMinValue(3L)

case class longMinValue(min: Long) extends ValidationRule {
  def name = "longMinValue"
  override def messageParams = Seq(min.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toLong >= min
}

// ----
// param("x" -> 1.0D) is longMaxValue(5L)

case class longMaxValue(max: Long) extends ValidationRule {
  def name = "longMaxValue"
  override def messageParams = Seq(max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toLong <= max
}

// ----
// param("x" -> "4D") is doubleMinMaxValue(3D, 5D)

case class doubleMinMaxValue(min: Double, max: Double) extends ValidationRule {
  def name = "doubleMinMaxValue"
  override def messageParams = Seq(min.toString, max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toDouble >= min && v.toString.toDouble <= max
}

// ----
// param("x" -> 2D) is doubleMinValue(3D)

case class doubleMinValue(min: Double) extends ValidationRule {
  def name = "doubleMinValue"
  override def messageParams = Seq(min.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toDouble >= min
}

// ----
// param("x" -> 6D) is doubleMaxValue(5D)

case class doubleMaxValue(max: Double) extends ValidationRule {
  def name = "doubleMaxValue"
  override def messageParams = Seq(max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toDouble <= max
}

// ----
// param("x" -> "4F") is floatMinMaxValue(3F, 5F)

case class floatMinMaxValue(min: Float, max: Float) extends ValidationRule {
  def name = "floatMinMaxValue"
  override def messageParams = Seq(min.toString, max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toFloat >= min && v.toString.toFloat <= max
}

// ----
// param("x" -> 2F) is floatMinValue(3F)

case class floatMinValue(min: Float) extends ValidationRule {
  def name = "floatMinValue"
  override def messageParams = Seq(min.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toFloat >= min
}

// ----
// param("x" -> 6F) is floatMaxValue(5F)

case class floatMaxValue(max: Float) extends ValidationRule {
  def name = "floatMaxValue"
  override def messageParams = Seq(max.toString)
  def isValid(v: Any) = isEmpty(v) || v.toString.toFloat <= max
}

// ----
// param("pair" -> ("pass", "pass")) are same

object same extends ValidationRule {
  def name = "same"
  def isValid(pair: Any) = {
    val (a, b) = pair.asInstanceOf[(Any, Any)]
    if (a.isInstanceOf[Option[_]] && b.isInstanceOf[Option[_]]) {
      val (x, y) = pair.asInstanceOf[(Option[Any], Option[Any])]
      (x.isEmpty && y.isEmpty) || (x.isDefined && y.isDefined && x.get == y.get)
    } else {
      a == b
    }
  }
}

// ----
// param("email" -> "alice@example.com") is email
// [NOTE] This is not a complete solution

object email extends ValidationRule {
  def name = "email"
  def isValid(v: Any) = isEmpty(v) ||
    """^([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})$""".r.findFirstIn(v.toString).isDefined
}

// ----
// param("time" -> new java.util.Date(123L)) is past
// param("time" -> org.joda.time.DateTime.now.minusDays(3)) is past

object past extends ValidationRule {
  def name = "past"
  def isValid(v: Any): Boolean = {
    if (v != null) {
      toHasGetTime(v) match {
        case Some(time) => time.getTime < nowMillis()
        case _ => false
      }
    } else false
  }
}

// ----
// param("time" -> new java.util.Date) is future

object future extends ValidationRule {
  def name = "future"
  def isValid(v: Any): Boolean = {
    if (v != null) {
      toHasGetTime(v) match {
        case Some(time) => time.getTime > nowMillis()
        case _ => false
      }
    } else false
  }
}

// ----
// param("createdAt" -> "2013-01-02 03:04:05") is dateTimeFormat
object dateTimeFormat extends ValidationRule {
  def name = "dateTimeFormat"
  def isValid(v: Any): Boolean = isEmpty(v) || {
    try DateTimeUtil.parseDateTime(v.toString) != null
    catch { case e: Exception => false }
  }
}

// ----
// param("birthday" -> "2011-06-22") is dateFormat
object dateFormat extends ValidationRule {
  def name = "dateFormat"
  def isValid(v: Any): Boolean = isEmpty(v) || {
    try DateTimeUtil.parseLocalDate(v.toString) != null
    catch { case e: Exception => false }
  }
}

// ----
// param("timeToWakeUp" -> "12:34:56") is timeFormat
object timeFormat extends ValidationRule {
  def name = "timeFormat"
  def isValid(v: Any): Boolean = isEmpty(v) || {
    try DateTimeUtil.parseLocalTime(v.toString) != null
    catch { case e: Exception => false }
  }
}

