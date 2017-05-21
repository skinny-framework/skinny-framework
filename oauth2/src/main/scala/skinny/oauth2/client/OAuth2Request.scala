package skinny.oauth2.client

import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import scala.collection.JavaConverters._

/**
  * OAuth 2.0 Request.
  */
case class OAuth2Request(underlying: OAuthClientRequest, provider: Option[OAuth2Provider] = None) {

  def body: Option[String] = Option(underlying.getBody)
  def body(body: String)   = underlying.setBody(body)

  def headers: Map[String, String]         = underlying.getHeaders.asScala.toMap
  def header(name: String): Option[String] = Option(underlying.getHeader(name))
  def header(name: String, value: String)  = underlying.setHeader(name, value)

  def locationURI: Option[String] = Option(underlying.getLocationUri)
  def locationURI(uri: String)    = underlying.setLocationUri(uri)

}
