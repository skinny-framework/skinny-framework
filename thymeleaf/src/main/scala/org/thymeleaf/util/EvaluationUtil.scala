/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2013, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package org.thymeleaf.util

import java.util.{ ArrayList => JArrayList }
import java.util.{ Collections => JCollections }
import java.util.{ List => JList }
import java.util.{ Map => JMap }
import java.lang.{ Iterable => JIterable }
import org.thymeleaf.standard.expression.LiteralValue
import scala.collection.JavaConverters._

/**
  * @author Daniel Fern&aacute;ndez
  * @since 2.1.0
  */
object EvaluationUtil {

  def evaluateAsBoolean(condition: AnyRef): Boolean = {
    condition match {
      case null                     => false
      case b: java.lang.Boolean     => b.booleanValue
      case bd: java.math.BigDecimal => bd.compareTo(java.math.BigDecimal.ZERO) != 0
      case bi: java.math.BigInteger => bi == java.math.BigInteger.ZERO
      case n: java.lang.Number      => n.doubleValue != 0.0
      case c: java.lang.Character   => c.charValue != 0
      case s: String =>
        val condStr: String = s.trim.toLowerCase
        !("false" == condStr || "off" == condStr || "no" == condStr)
      case l: LiteralValue =>
        val condStr: String = l.getValue.trim.toLowerCase
        !("false" == condStr || "off" == condStr || "no" == condStr)
      case _ => true
    }
  }

  def evaluateAsNumber(obj: AnyRef): java.math.BigDecimal = {
    obj match {
      case null                    => null
      case v: java.math.BigDecimal => v
      case v: java.math.BigInteger => new java.math.BigDecimal(v)
      case v: java.lang.Short      => new java.math.BigDecimal(v.intValue)
      case v: java.lang.Integer    => new java.math.BigDecimal(v.intValue)
      case v: java.lang.Long       => new java.math.BigDecimal(v.longValue)
      case v: java.lang.Float      => new java.math.BigDecimal(v.doubleValue)
      case v: java.lang.Double     => new java.math.BigDecimal(v.doubleValue)
      case v: String =>
        try new java.math.BigDecimal(v.trim)
        catch { case ignored: NumberFormatException => null }
      case _ => null
    }
  }

  private[this] def toMapEntry(key: Any, value: Any) = {
    new MapEntry[AnyRef, AnyRef](key.asInstanceOf[AnyRef], value.asInstanceOf[AnyRef])
  }

  def evaluateAsIterable(value: AnyRef): JList[AnyRef] = {
    val result: JList[AnyRef] = new JArrayList[AnyRef]
    value match {
      case null =>
      // just added scala.Iterable support
      case iter: scala.Iterable[_] => result.addAll(iter.map(_.asInstanceOf[AnyRef]).asJavaCollection)
      case jiter: JIterable[_] =>
        jiter.asScala.foreach { obj =>
          result.add(obj.asInstanceOf[AnyRef])
        }
      case jmap: JMap[_, _] =>
        jmap.entrySet.asScala.map { e =>
          result.add(toMapEntry(e.getKey, e.getValue))
        }
      case array if array.getClass.isArray =>
        array match {
          case a: Array[Byte] =>
            a.foreach { obj =>
              result.add(java.lang.Byte.valueOf(obj))
            }
          case a: Array[Short] =>
            a.foreach { obj =>
              result.add(java.lang.Short.valueOf(obj))
            }
          case a: Array[Int] =>
            a.foreach { obj =>
              result.add(java.lang.Integer.valueOf(obj))
            }
          case a: Array[Long] =>
            a.foreach { obj =>
              result.add(java.lang.Long.valueOf(obj))
            }
          case a: Array[Float] =>
            a.foreach { obj =>
              result.add(java.lang.Float.valueOf(obj))
            }
          case a: Array[Double] =>
            a.foreach { obj =>
              result.add(java.lang.Double.valueOf(obj))
            }
          case a: Array[Boolean] =>
            a.foreach { obj =>
              result.add(java.lang.Boolean.valueOf(obj))
            }
          case a: Array[Char] =>
            a.foreach { obj =>
              result.add(java.lang.Character.valueOf(obj))
            }
          case a: Array[AnyRef] => JCollections.addAll(result, a)
        }
      case _ => result.add(value)
    }
    JCollections.unmodifiableList(result)
  }

  def evaluateAsArray(value: AnyRef): Array[AnyRef] = {
    val result: JList[AnyRef] = new JArrayList[AnyRef]
    if (value == null) {
      return Array[AnyRef](null)
    }
    value match {
      // just added scala.Iterable support
      case iter: Iterable[_] =>
        iter.foreach { obj =>
          result.add(obj.asInstanceOf[AnyRef])
        }
      case jiter: JIterable[_] =>
        jiter.asScala.foreach { obj =>
          result.add(obj.asInstanceOf[AnyRef])
        }
      case jmap: JMap[_, _] =>
        jmap.entrySet.asScala.foreach { obj =>
          result.add(obj)
        }
      case a if value.getClass.isArray => a.asInstanceOf[Array[AnyRef]]
      case _                           => result.add(value)
    }
    result.toArray(new Array[AnyRef](result.size))
  }

  class MapEntry[K, V](val key: K, val value: V) extends java.util.Map.Entry[K, V] {
    def getKey: K             = key
    def getValue: V           = value
    def setValue(value: V): V = throw new UnsupportedOperationException

    override def toString: String = s"${key}=${value}"

    override def equals(o: Any): Boolean = {
      if (this == o) {
        return true
      }
      if (!(o.isInstanceOf[JMap.Entry[_, _]])) {
        return false
      }
      val mapEntry: JMap.Entry[_, _] = o.asInstanceOf[JMap.Entry[_, _]]
      if (if (key != null) !(key == mapEntry.getKey) else mapEntry.getKey != null) {
        return false
      }
      if (if (value != null) !(value == mapEntry.getValue) else mapEntry.getValue != null) {
        return false
      }
      return true
    }

    override def hashCode: Int = {
      (31 * (if (key != null) key.hashCode else 0)) +
      (if (value != null) value.hashCode else 0)
    }
  }

}
