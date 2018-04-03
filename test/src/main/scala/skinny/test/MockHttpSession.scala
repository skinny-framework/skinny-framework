/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skinny.test

import java.util._
import javax.servlet.ServletContext
import javax.servlet.http._

class MockHttpSession extends HttpSession {

  var nextId: Int                     = 1
  var id: String                      = null
  val creationTime: Long              = System.currentTimeMillis
  var maxInactiveInterval: Int        = 0
  var lastAccessedTime: Long          = System.currentTimeMillis
  val servletContext: ServletContext  = null
  val attributes: Map[String, AnyRef] = new LinkedHashMap[String, AnyRef]
  var invalid: Boolean                = false
  var isNewSession: Boolean           = true

  override def getCreationTime: Long = {
    this.creationTime
  }

  override def getId: String = {
    this.id
  }

  def access: Unit = {
    this.lastAccessedTime = System.currentTimeMillis
    this.isNewSession = false
  }

  override def getLastAccessedTime: Long = {
    this.lastAccessedTime
  }

  override def getServletContext: ServletContext = {
    this.servletContext
  }

  override def setMaxInactiveInterval(interval: Int): Unit = {
    this.maxInactiveInterval = interval
  }

  override def getMaxInactiveInterval: Int = {
    this.maxInactiveInterval
  }

  override def getSessionContext: HttpSessionContext = throw new UnsupportedOperationException

  override def getAttribute(name: String): AnyRef = {
    this.attributes.get(name)
  }

  override def getValue(name: String): AnyRef = {
    getAttribute(name)
  }

  override def getAttributeNames: Enumeration[String] = {
    Collections.enumeration(new LinkedHashSet[String](this.attributes.keySet))
  }

  override def getValueNames: Array[String] = {
    this.attributes.keySet.toArray(new Array[String](this.attributes.size))
  }

  override def setAttribute(name: String, value: AnyRef): Unit = {
    if (value != null) {
      this.attributes.put(name, value)
      if (value.isInstanceOf[HttpSessionBindingListener]) {
        (value.asInstanceOf[HttpSessionBindingListener]).valueBound(new HttpSessionBindingEvent(this, name, value))
      }
    } else {
      removeAttribute(name)
    }
  }

  override def putValue(name: String, value: AnyRef): Unit = {
    setAttribute(name, value)
  }

  override def removeAttribute(name: String): Unit = {
    val value: AnyRef = this.attributes.remove(name)
    if (value.isInstanceOf[HttpSessionBindingListener]) {
      (value.asInstanceOf[HttpSessionBindingListener]).valueUnbound(new HttpSessionBindingEvent(this, name, value))
    }
  }

  override def removeValue(name: String) = removeAttribute(name)

  def clearAttributes = attributes.clear()

  override def invalidate: Unit = {
    this.invalid = true
    clearAttributes
  }

  def isInvalid: Boolean = this.invalid

  override def isNew: Boolean = {
    this.isNewSession
  }

}
