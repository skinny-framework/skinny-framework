package org.thymeleaf.util

import org.scalatest._
import org.thymeleaf.util.EvaluationUtil._

class EvaluationUtilSpec extends FlatSpec with Matchers {

  it should "evaluate as boolean value" in {
    {
      val v: String = null
      EvaluationUtil.evaluateAsBoolean(v) should equal(false)
    }
    {
      val v = "true"
      EvaluationUtil.evaluateAsBoolean(v) should equal(true)
    }
    {
      val v = "false"
      EvaluationUtil.evaluateAsBoolean(v) should equal(false)
    }

    {
      val v: java.lang.Integer = 0
      EvaluationUtil.evaluateAsBoolean(v) should equal(false)
    }
    {
      val v: java.lang.Integer = 1
      EvaluationUtil.evaluateAsBoolean(v) should equal(true)
    }
  }

  it should "evaluate as number value" in {
    {
      val v: String = null
      EvaluationUtil.evaluateAsNumber(v) should equal(null)
    }
    {
      val v = "true"
      EvaluationUtil.evaluateAsNumber(v) should equal(null)
    }
    {
      val v: java.lang.Integer = -1
      EvaluationUtil.evaluateAsNumber(v) should equal(new java.math.BigDecimal(-1))
    }
    {
      val v: java.lang.Integer = 0
      EvaluationUtil.evaluateAsNumber(v) should equal(new java.math.BigDecimal(0))
    }
    {
      val v: java.lang.Integer = 1
      EvaluationUtil.evaluateAsNumber(v) should equal(new java.math.BigDecimal(1))
    }
  }

  it should "evaluate as Array value" in {
    {
      val v: String = null
      EvaluationUtil.evaluateAsArray(v) should equal(Array[AnyRef](null))
    }
    {
      val v = Array[String]()
      EvaluationUtil.evaluateAsArray(v) should equal(v)
    }
  }

  it should "evaluate as Iterable value" in {
    {
      val v: String = null
      EvaluationUtil.evaluateAsIterable(v).size should equal(0)
    }
    {
      val v = Seq(1, 2, 3)
      EvaluationUtil.evaluateAsIterable(v).size should equal(3)
    }
  }

  "MapEntry" should "be available" in {
    val entity = new MapEntry[String, Int]("foo", 123)
    entity.getKey should equal("foo")
    entity.getValue should equal(123)

    intercept[UnsupportedOperationException] {
      entity.setValue(234)
    }

    entity.toString should equal("foo=123")

    entity.hashCode

    val entity2 = new MapEntry[String, Int]("foo", 123)
    // TODO: Fix stackoverflow
    //entity.equals(entity2) should equal(true)
  }

}
