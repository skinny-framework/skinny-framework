package skinny.oauth2.client.typetalk

import org.joda.time.DateTime
import skinny.oauth2.client.OAuth2User

case class TypetalkUser(
  override val id: String,
  name: String,
  fullName: String,
  suggestion: String,
  imageUrl: String,
  createdAt: DateTime,
  updatedAt: DateTime) extends OAuth2User

