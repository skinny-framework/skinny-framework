package skinny.oauth2.client

import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest
import org.apache.oltu.oauth2.common.OAuth

import scala.collection.concurrent.TrieMap
import scala.collection.JavaConverters._
import scala.language.reflectiveCalls

object BearerRequest {

  def apply(url: String): BearerRequest = new BearerRequest(url, null)

}

case class BearerRequest(resourceUrl: String, accessToken: String) {

  val underlying = new OAuthBearerClientRequest(resourceUrl) {
    def param(name: String, value: String) = this.parameters.put(name, value)
  }

  trait OAuthRequestWithMethod extends OAuth2Request {
    val method: HttpMethod
  }

  private[this] var method: HttpMethod = HttpMethod.GET

  private[this] val headers: TrieMap[String, String] = new scala.collection.concurrent.TrieMap[String, String]()

  private[this] var body: Option[String] = None

  def accessToken(accessToken: String): BearerRequest = {
    underlying.setAccessToken(accessToken)
    this
  }

  def refreshToken(refreshToken: String): BearerRequest = {
    param(OAuth.OAUTH_REFRESH_TOKEN, refreshToken)
    this
  }

  def method(method: HttpMethod): BearerRequest = {
    this.method = method
    this
  }

  def build(): OAuthRequestWithMethod = {
    val req = new OAuth2Request(underlying.buildQueryMessage()) with OAuthRequestWithMethod {
      override val method = BearerRequest.this.method
    }
    req.underlying.setHeaders(headers.asJava)
    body.foreach(req.underlying.setBody)
    req
  }

  def param(name: String, value: AnyRef): BearerRequest = {
    if (value != null) {
      underlying.param(name, String.valueOf(value))
    }
    this
  }

  def header(name: String, value: String): BearerRequest = {
    this.headers.put(name, value)
    this
  }

  def body(body: String): BearerRequest = {
    this.body = Option(body)
    this
  }

}
