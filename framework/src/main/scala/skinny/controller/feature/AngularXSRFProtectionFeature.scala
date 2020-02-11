package skinny.controller.feature

import skinny.micro.SkinnyMicroBase
import skinny.logging.LoggerProvider

/**
  * Angular.js Cross Site Request Forgery (XSRF) Protection support.
  *
  * https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
  */
trait AngularXSRFProtectionFeature extends AngularXSRFCookieProviderFeature {

  self: SkinnyMicroBase
    with ActionDefinitionFeature
    with BeforeAfterActionFeature
    with RequestScopeFeature
    with LoggerProvider =>

  /**
    * Enabled if true.
    */
  private[this] var forgeryProtectionEnabled: Boolean = false

  /**
    * Excluded actions.
    */
  private[this] val forgeryProtectionExcludedActionNames = new scala.collection.mutable.ArrayBuffer[String]

  /**
    * Included actions.
    */
  private[this] val forgeryProtectionIncludedActionNames = new scala.collection.mutable.ArrayBuffer[String]

  /**
    * Cookie name.
    */
  override protected def xsrfCookieName: String = super.xsrfCookieName

  /**
    * Header name.
    */
  protected def xsrfHeaderName: String = AngularJSSpecification.xsrfHeaderName

  /**
    * Declarative activation of XSRF protection. Of course, highly inspired by Ruby on Rails.
    *
    * @param only should be applied only for these action methods
    * @param except should not be applied for these action methods
    */
  def protectFromForgery(only: Seq[String] = Nil, except: Seq[String] = Nil): Unit = {
    forgeryProtectionEnabled = true
    forgeryProtectionIncludedActionNames ++= only
    forgeryProtectionExcludedActionNames ++= except
  }

  /**
    * Overrides to skip execution when the current request matches excluded patterns.
    */
  def handleAngularForgery(): Unit = {
    if (forgeryProtectionEnabled) {
      logger.debug {
        s"""
        | ------------------------------------------
        |  [Angular XSRF Protection Enabled]
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
    * Handles when XSRF is detected.
    */
  def handleForgeryIfDetected(): Unit = halt(403)

  def isForged: Boolean = {
    implicit val ctx   = context
    val unsafeMethod   = !request.requestMethod.isSafe
    val headerValue    = request.headers.get(xsrfHeaderName)
    val cookieValue    = request.cookies.get(xsrfCookieName)
    val neither        = (headerValue.isEmpty || cookieValue.isEmpty)
    val differentValue = !headerValue.exists(h => cookieValue.exists(c => c == h))
    unsafeMethod && (neither || differentValue)
  }

  before(isForged) {
    handleAngularForgery()
  }

}
