package skinny.oauth2.client.dropbox

import skinny.oauth2.client.OAuth2User

private[skinny] case class RawDropboxUser(
    uid: Long,
    displayName: String,
    nameDetails: NameDetails,
    referralLink: String,
    country: String,
    locale: String,
    email: String,
    emailVerified: Boolean,
    isPaired: Boolean,
    team: Option[Team],
    quotaInfo: QuotaInfo
) {

  def toDropboxUser: DropboxUser = new DropboxUser(
    id = uid.toString,
    displayName = displayName,
    nameDetails = nameDetails,
    referralLink = referralLink,
    country = country,
    locale = locale,
    email = email,
    emailVerified = emailVerified,
    isPaired = isPaired,
    team = team,
    quotaInfo = quotaInfo
  )
}

/**
 * Authorized Dropbox user basic information.
 */
case class DropboxUser(
  override val id: String,
  displayName: String,
  nameDetails: NameDetails,
  referralLink: String,
  country: String,
  locale: String,
  email: String,
  emailVerified: Boolean,
  isPaired: Boolean,
  team: Option[Team],
  quotaInfo: QuotaInfo
)
    extends OAuth2User
