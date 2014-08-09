package skinny.oauth2.client

import org.apache.oltu.oauth2.common.message.types.{ GrantType => OltuGrantType }

/**
 * Grant Type.
 */
case class GrantType(value: String) {

  def toOltuEnum(): OltuGrantType = value match {
    case v if v == OltuGrantType.AUTHORIZATION_CODE.name() => OltuGrantType.AUTHORIZATION_CODE
    case v if v == OltuGrantType.CLIENT_CREDENTIALS.name() => OltuGrantType.CLIENT_CREDENTIALS
    case v if v == OltuGrantType.PASSWORD.name() => OltuGrantType.PASSWORD
    case v if v == OltuGrantType.REFRESH_TOKEN.name() => OltuGrantType.REFRESH_TOKEN
  }
}

object GrantType {
  def apply(t: OltuGrantType): GrantType = new GrantType(t.name())

  val AuthorizationCode = GrantType(OltuGrantType.AUTHORIZATION_CODE)
  val ClientCredentials = GrantType(OltuGrantType.CLIENT_CREDENTIALS)
  val Password = GrantType(OltuGrantType.PASSWORD)
  val RefreshToken = GrantType(OltuGrantType.REFRESH_TOKEN)
}
