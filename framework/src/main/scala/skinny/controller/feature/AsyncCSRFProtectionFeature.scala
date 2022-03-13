package skinny.controller.feature

import skinny.micro.SkinnyMicroBase
import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.{ AsyncCSRFTokenSupport, CSRFTokenSupport }
import skinny.logging.LoggerProvider

object AsyncCSRFProtectionFeature {

  // follows Rails default
  val DEFAULT_KEY: String = "csrf-token"

}

/**
  * Provides Cross-Site Request Forgery (CSRF) protection.
  */
trait AsyncCSRFProtectionFeature extends AsyncCSRFTokenSupport {

  self: SkinnyMicroBase
    with ActionDefinitionFeature
    with AsyncBeforeAfterActionFeature
    with RequestScopeFeature
    with LoggerProvider =>

  /**
    * Overrides Scalatra's default key name.
    */
  override def csrfKey: String = CSRFProtectionFeature.DEFAULT_KEY

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
    * Declarative activation of CSRF protection. Of course, highly inspired by Ruby on Rails.
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
  override def handleForgery()(implicit ctx: SkinnyContext): Unit = {
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
  beforeAction() { implicit ctx =>
    if (getFromRequestScope(RequestScopeFeature.ATTR_CSRF_KEY)(context).isEmpty) {
      set(RequestScopeFeature.ATTR_CSRF_KEY, csrfKey)(context)
      set(RequestScopeFeature.ATTR_CSRF_TOKEN, prepareCsrfToken())(context)
    }
  }

}
