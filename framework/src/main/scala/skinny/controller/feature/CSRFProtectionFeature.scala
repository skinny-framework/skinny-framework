package skinny.controller.feature

import org.scalatra._
import grizzled.slf4j.Logging

trait CSRFProtectionFeature extends CsrfTokenSupport { self: ScalatraBase with BasicFeature with RequestScopeFeature with Logging =>

  private[this] var forgeryProtectionEnabled: Boolean = false
  private[this] val forgeryProtectionExcludedActionNames = new scala.collection.mutable.ArrayBuffer[Symbol]
  private[this] val forgeryProtectionIncludedActionNames = new scala.collection.mutable.ArrayBuffer[Symbol]

  override def csrfKey: String = "csrfToken"

  def protectFromForgery(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil) {
    forgeryProtectionEnabled = true
    forgeryProtectionIncludedActionNames ++= only
    forgeryProtectionExcludedActionNames ++= except
  }

  override def handleForgery() {
    if (forgeryProtectionEnabled) {
      logger.debug {
        s"""
        | ------------------------------------------
        |  [CSRF Protection Enabled]
        |  method      : ${request.getMethod}
        |  requestPath : ${requestPath}
        |  actionName  : ${currentActionName}
        |  only        : ${forgeryProtectionIncludedActionNames.mkString(", ")}
        |  except      : ${forgeryProtectionExcludedActionNames.mkString(", ")}
        | ------------------------------------------
        |""".stripMargin
      }

      currentActionName.map { name =>
        val currentPathShouldBeExcluded = forgeryProtectionExcludedActionNames.exists(_ == name)
        if (!currentPathShouldBeExcluded) {
          val allPathShouldBeIncluded = forgeryProtectionIncludedActionNames.isEmpty
          val currentPathShouldBeIncluded = forgeryProtectionIncludedActionNames.exists(_ == name)
          if (allPathShouldBeIncluded || currentPathShouldBeIncluded) {
            handleForgeryIfDetected()
          }
        }
      }.getOrElse {
        handleForgeryIfDetected()
      }
    }
  }

  // TODO default implementation
  def handleForgeryIfDetected(): Unit = super.handleForgery()

  before() {
    if (requestScope("csrfKey").isEmpty) {
      set("csrfKey", csrfKey)
      set("csrfToken", csrfToken)
    }
  }

}
