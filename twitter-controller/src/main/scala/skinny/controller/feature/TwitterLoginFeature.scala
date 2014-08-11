package skinny.controller.feature

import skinny.controller.SkinnyControllerBase
import twitter4j._
import twitter4j.auth._
import twitter4j.conf._

/**
 * Twitter OAuth 1.0a Login Feature.
 */
trait TwitterLoginFeature extends SkinnyControllerBase {

  protected def isLocalDebug: Boolean = false

  protected def isTwitter4jDebug: Boolean = true

  // ----------------------------------------
  // Env names

  protected def consumerKeyEnvName: String = "SKINNY_OAUTH1_CONSUMER_KEY_TWITTER"

  protected def consumerSecretEnvName: String = "SKINNY_OAUTH1_CONSUMER_SECRET_TWITTER"

  // ----------------------------------------
  // Session keys

  protected def sessionRequestTokenName: String = "SKINNY_OAUTH1_REQUEST_TOKEN_TWITTER"

  protected def sessionRequestTokenSecretName: String = "SKINNY_OAUTH1_REQUEST_TOKEN_SECRET_TWITTER"

  protected def sessionAccessTokenName: String = "SKINNY_OAUTH1_ACCESS_TOKEN_TWITTER"

  protected def sessionAccessTokenSecretName: String = "SKINNY_OAUTH1_ACCESS_TOKEN_SECRET_TWITTER"

  // ----------------------------------------
  // OAuth 1.0a consumer key/secret

  protected def consumerKey: String = sys.env(consumerKeyEnvName)

  protected def consumerSecret: String = sys.env(consumerSecretEnvName)

  // ----------------------------------------
  // Request Token

  protected def saveRequestToken(requestToken: RequestToken): Unit = {
    session.setAttribute(sessionRequestTokenName, requestToken.getToken)
    session.setAttribute(sessionRequestTokenSecretName, requestToken.getTokenSecret)
  }

  protected def currentRequestToken(): Option[RequestToken] = {
    for {
      token <- Option(session.getAttribute(sessionRequestTokenName)).map(_.toString)
      tokenSecret <- Option(session.getAttribute(sessionRequestTokenSecretName)).map(_.toString)
    } yield {
      new RequestToken(token, tokenSecret)
    }
  }

  protected def deleteSavedRequestToken(): Unit = {
    session.removeAttribute(sessionRequestTokenName)
    session.removeAttribute(sessionRequestTokenSecretName)
  }

  // ----------------------------------------
  // Access Token

  protected def saveAccessToken(accessToken: AccessToken): Unit = {
    println("SSS"+ accessToken.getToken)
    session.setAttribute(sessionAccessTokenName, accessToken.getToken)
    session.setAttribute(sessionAccessTokenSecretName, accessToken.getTokenSecret)
  }

  protected def currentAccessToken(): Option[AccessToken] = {
    for {
      token <- Option(session.getAttribute(sessionAccessTokenName)).map(_.toString)
      tokenSecret <- Option(session.getAttribute(sessionAccessTokenSecretName)).map(_.toString)
    } yield {
      new AccessToken(token, tokenSecret)
    }
  }

  protected def deleteSavedAccessToken(): Unit = {
    session.removeAttribute(sessionAccessTokenName)
    session.removeAttribute(sessionAccessTokenSecretName)
  }

  // ----------------------------------------
  // event handlers

  protected def saveAuthorizedUser(user: User): Unit

  protected def handleWhenLoginFailed(): Any = haltWithBody(401)

  protected def handleWhenLoginSucceeded(): Any

  // ----------------------------------------
  // Twitter4j

  protected lazy val twitter4jConfiguration: Configuration = {
    new ConfigurationBuilder()
      .setDebugEnabled(isTwitter4jDebug)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .build()
  }

  protected lazy val twitterFactory: TwitterFactory = {
    new TwitterFactory(twitter4jConfiguration)
  }

  protected def twitter: Option[Twitter] = currentAccessToken.map(t => twitter(t)) 

  protected def twitter(accessToken: AccessToken): Twitter = twitterFactory.getInstance(accessToken)

  // ----------------------------------------
  // Actions

  /**
   * Redirects users to OAuth provider's authentication endpoint.
   */
  def loginRedirect: Any = {
    val twitter: Twitter = twitterFactory.getInstance
    val requestToken: RequestToken = twitter.getOAuthRequestToken
    logger.debug(s"request token when redirecting: ${requestToken}")
    saveRequestToken(requestToken)
    redirect(requestToken.getAuthorizationURL)
  }

  /**
   * Accepts callback response from OAuth provider.
   */
  def callback: Any = {
    logger.debug(s"request token in session: ${currentRequestToken}")
    logger.debug(s"oauth_token: ${params.get("oauth_token")}, oauth_verifier: ${params.get("oauth_verifier")}")

    try {
      (for {
        requestToken <- currentRequestToken
        token <- params.getAs[String]("oauth_token") if token == requestToken.getToken
        verifier <- params.getAs[String]("oauth_verifier")
      } yield {

        val twitter: Twitter = twitterFactory.getInstance
        // access token retrieval
        val accessToken: AccessToken = try {
          twitter.getOAuthAccessToken(requestToken, verifier)
        } catch { case e: TwitterException =>
          logger.error(s"Failed to retrieve access token because ${e.getMessage}", e)
          handleWhenLoginFailed()
          haltWithBody(401) // will be dead code
        }
        saveAccessToken(accessToken)
        twitter.setOAuthAccessToken(accessToken)

        // retrieve current user information
        val user: User = try {
          twitter.showUser(twitter.getId)
        } catch { case e: TwitterException =>
          logger.error(s"Failed to retrieve user information because ${e.getMessage}", e)
          handleWhenLoginFailed()
          haltWithBody(401) // will be dead code
        }
        saveAuthorizedUser(user)

        // login succeeded
        handleWhenLoginSucceeded()

      }).getOrElse {
        handleWhenLoginFailed()
      }

    } finally {
      if (!isLocalDebug) {
        deleteSavedRequestToken()
      }
    }
  }

}
