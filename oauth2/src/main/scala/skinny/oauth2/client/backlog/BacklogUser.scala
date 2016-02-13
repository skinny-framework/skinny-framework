package skinny.oauth2.client.backlog

import skinny.oauth2.client.OAuth2User

case class BacklogUser(
  id: String,
  userId: Option[String] = None,
  name: String,
  roleType: Int,
  lang: Option[String] = None,
  mailAddress: Option[String] = None
) extends OAuth2User
