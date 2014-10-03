package skinny.oauth2.client.github

import skinny.oauth2.client.OAuth2User

/**
 * Authorized GitHub user basic information.
 */
case class GitHubUser(
  override val id: String,
  avatarUrl: String,
  login: String,
  url: String,
  name: String,
  email: Option[String]) extends OAuth2User
