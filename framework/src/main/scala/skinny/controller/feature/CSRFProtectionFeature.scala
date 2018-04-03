package skinny.controller.feature

import skinny.micro.SkinnyMicroBase
import skinny.micro.contrib.CSRFTokenSupport
import skinny.logging.LoggerProvider

object CSRFProtectionFeature {

  // follows Rails default
  val DEFAULT_KEY: String = "csrf-token"

}

/**
  * Provides Cross-Site Request Forgery (CSRF) protection.
  */
trait CSRFProtectionFeature extends CSRFTokenSupport {

  self: SkinnyMicroBase
    with ActionDefinitionFeature
    with BeforeAfterActionFeature
    with RequestScopeFeature
    with LoggerProvider =>

  /**
    * Overrides Skinny Micro's default key name.
    */
  override def csrfKey: String = CSRFProtectionFeature.DEFAULT_KEY

  /**
    * Enabled if true.
    */
  private[this] var forgeryProtectionEnabled: Boolean = false

  /**
    * Excluded actions.
    */
  private[this] val forgeryProtectionExcludedActionNames = new scala.collection.mutable.ArrayBuffer[Symbol]

  /**
    * Included actions.
    */
  private[this] val forgeryProtectionIncludedActionNames = new scala.collection.mutable.ArrayBuffer[Symbol]

  /**
    * Declarative activation of CSRF protection. Of course, highly inspired by Ruby on Rails.
    *
    * @param only should be applied only for these action methods
    * @param except should not be applied for these action methods
    */
  def protectFromForgery(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil): Unit = {
    forgeryProtectionEnabled = true
    forgeryProtectionIncludedActionNames ++= only
    forgeryProtectionExcludedActionNames ++= except
  }

  /**
    * Overrides to skip execution when the current request matches excluded patterns.
    */
  override def handleForgery(): Unit = {
    if (forgeryProtectionEnabled) {
      logger.debug {
        s"""
        | ------------------------------------------
        |  [CSRF Protection Enabled]
        |  method      : ${request(context).getMethod}
        |  requestPath : ${requestPath(context)}
        |  actionName  : ${currentActionName}
        |  only        : ${forgeryProtectionIncludedActionNames.mkString(", ")}
        |  except      : ${forgeryProtectionExcludedActionNames.mkString(", ")}
        | ------------------------------------------
        |""".stripMargin
      }

      currentActionName
        .map { name =>
          val currentPathShouldBeExcluded = forgeryProtectionExcludedActionNames.exists(_ == name)
          if (!currentPathShouldBeExcluded) {
            val allPathShouldBeIncluded     = forgeryProtectionIncludedActionNames.isEmpty
            val currentPathShouldBeIncluded = forgeryProtectionIncludedActionNames.exists(_ == name)
            if (allPathShouldBeIncluded || currentPathShouldBeIncluded) {
              handleForgeryIfDetected()
            }
          }
        }
        .getOrElse {
          handleForgeryIfDetected()
        }
    }
  }

  /**
    * Handles when CSRF is detected.
    */
  def handleForgeryIfDetected(): Unit = halt(403)

  // Registers csrfKey & csrfToken to request scope.
  beforeAction() {
    if (getFromRequestScope(RequestScopeFeature.ATTR_CSRF_KEY)(context).isEmpty) {
      set(RequestScopeFeature.ATTR_CSRF_KEY, csrfKey)(context)
      set(RequestScopeFeature.ATTR_CSRF_TOKEN, prepareCsrfToken())(context)
    }
  }

}
