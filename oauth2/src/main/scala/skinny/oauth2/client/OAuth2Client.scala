package skinny.oauth2.client

import org.apache.oltu.oauth2.client._
import org.apache.oltu.oauth2.client.response._
import org.apache.oltu.oauth2.client.{ OAuthClient => OltuOAuthClient }

object OAuth2Client extends OAuth2Client

/**
  * OAuth2 Client.
  */
trait OAuth2Client {

  /**
    * Apache Oltu Client.
    */
  val client: OltuOAuthClient = new OltuOAuthClient(new URLConnectionClient())

  /**
    * Retrieves access token.
    */
  def accessToken(request: AccessTokenRequest): AccessTokenResponse = {
    val _req = request.build()
    AccessTokenResponse(
      if (_req.provider.exists(_.isJsonResponse)) {
        client.accessToken(_req.underlying, classOf[OAuthJSONAccessTokenResponse])
      } else {
        client.accessToken(_req.underlying, classOf[GitHubTokenResponse])
      }
    )
  }

  /**
    * Send a bearer request to retrieve resources.
    */
  def resource(request: BearerRequest): ResourceResponse = {
    val _req = request.build()
    ResourceResponse(client.resource(_req.underlying, _req.method.value, classOf[OAuthResourceResponse]))
  }

}
