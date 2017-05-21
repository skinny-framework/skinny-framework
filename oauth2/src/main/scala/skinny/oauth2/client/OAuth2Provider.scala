package skinny.oauth2.client

import org.apache.oltu.oauth2.common.OAuthProviderType

/**
  * OAuth 2.0 Provider.
  */
case class OAuth2Provider(
    providerName: String,
    authorizationEndpoint: String,
    accessTokenEndpoint: String,
    isJsonResponse: Boolean = true
)

object OAuth2Provider {

  def apply(providerType: OAuthProviderType): OAuth2Provider = {
    providerType match {
      case OAuthProviderType.FACEBOOK | OAuthProviderType.GITHUB =>
        new OAuth2Provider(
          providerType.getProviderName,
          providerType.getAuthzEndpoint,
          providerType.getTokenEndpoint,
          false
        )
      case _ =>
        new OAuth2Provider(
          providerType.getProviderName,
          providerType.getAuthzEndpoint,
          providerType.getTokenEndpoint
        )
    }
  }

  // ---------------------------------------
  // Supported OAuth2 Providers

  // Apache Oltu built-in providers
  val Facebook   = OAuth2Provider(OAuthProviderType.FACEBOOK)
  val Foursquare = OAuth2Provider(OAuthProviderType.FOURSQUARE)
  val Google     = OAuth2Provider(OAuthProviderType.GOOGLE)
  val GitHub     = OAuth2Provider(OAuthProviderType.GITHUB)
  val Instagram  = OAuth2Provider(OAuthProviderType.INSTAGRAM)
  val LinkedIn   = OAuth2Provider(OAuthProviderType.LINKEDIN)
  val Microsoft  = OAuth2Provider(OAuthProviderType.MICROSOFT)
  val PayPal     = OAuth2Provider(OAuthProviderType.PAYPAL)
  val Reddit     = OAuth2Provider(OAuthProviderType.REDDIT)
  val Salesforce = OAuth2Provider(OAuthProviderType.SALESFORCE)
  val Yammer     = OAuth2Provider(OAuthProviderType.YAMMER)

  // Basecamp
  // https://github.com/basecamp/api/blob/master/sections/authentication.md
  val Basecamp = OAuth2Provider(
    "basecamp",
    "https://launchpad.37signals.com/authorization/new",
    "https://launchpad.37signals.com/authorization/token"
  )

  // Dropbox
  // https://www.dropbox.com/developers/core/docs#oa2-authorize
  val Dropbox = OAuth2Provider(
    "dropbox",
    "https://www.dropbox.com/1/oauth2/authorize",
    "https://www.dropbox.com/1/oauth2/token"
  )

  // mixi
  // http://developer.mixi.co.jp/en/connect/mixi_graph_api/api_auth/
  val Mixi = OAuth2Provider(
    "mixi",
    "https://mixi.jp/connect_authorize.pl",
    "https://secure.mixi-platform.com/2/token"
  )

  // YConnect (Yahoo! JAPAN)
  // http://developer.yahoo.co.jp/yconnect/ (Japanese)
  val YConnect = OAuth2Provider(
    "yconnect",
    "https://auth.login.yahoo.co.jp/yconnect/v1/authorization",
    "https://auth.login.yahoo.co.jp/yconnect/v1/token"
  )

  // Typetalk
  // https://www.typetalk.in/
  val Typetalk = OAuth2Provider(
    "typetalk",
    "https://typetalk.in/oauth2/authorize",
    "https://typetalk.in/oauth2/access_token"
  )

  // Project Management Tool "Backlog"
  // http://backlogtool.com/
  val Backlog = OAuth2Provider(
    "backlog",
    "https://{space}.backlogtool.com/OAuth2AccessRequest.action",
    "https://{space}.backlogtool.com/api/v2/oauth2/token"
  )
  // プロジェクト管理ツール"Backlog"
  // http://www.backlog.jp/
  val BacklogJP = OAuth2Provider(
    "backlog_jp",
    "https://{space}.backlog.jp/OAuth2AccessRequest.action",
    "https://{space}.backlog.jp/api/v2/oauth2/token"
  )

  // *** IMPORTANT ***
  // Waiting for your pull request here!
  /*
  val YourFavoriteProvider = OAuthProvider(
    "providerName",
    "the authorization endpoint",
    "the access token endpoint"
  )
 */

}
