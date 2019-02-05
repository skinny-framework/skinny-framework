package skinny.oauth2.client.google

import skinny.oauth2.client.OAuth2User

/**
  * Authorized Google OpenID Connect user
  */
case class GoogleOpenIDConnectUser(
    sub: String,
    email: Option[String],
    emailVerified: Option[Boolean],
    name: Option[String],
    familyName: Option[String],
    givenName: Option[String],
    locale: Option[String],
    picture: Option[String]
) extends OAuth2User {
  override val id: String = sub
}
