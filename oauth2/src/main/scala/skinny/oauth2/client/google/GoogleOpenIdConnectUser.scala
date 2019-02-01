package skinny.oauth2.client.google

import skinny.oauth2.client.OAuth2User

/**
  * Authorized Google OpenID Connect user
  */
case class GoogleOpenIdConnectUser(
    sub: String,
    email: String,
    emailVerified: Boolean,
    name: String,
    familyName: String,
    givenName: String,
    locale: String,
    picture: String
) extends OAuth2User {
  override val id: String = sub
}
