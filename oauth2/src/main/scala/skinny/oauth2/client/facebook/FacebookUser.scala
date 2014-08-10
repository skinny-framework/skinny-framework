package skinny.oauth2.client.facebook

import skinny.oauth2.client.OAuth2User

/**
 * Authorized Facebook user basic information.
 */
case class FacebookUser(
  override val id: String,
  name: String,
  firstName: String,
  middleName: Option[String],
  lastName: String,
  email: Option[String],
  link: String,
  website: Option[String]) extends OAuth2User
