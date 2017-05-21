package skinny.oauth2.client

import org.apache.oltu.oauth2.client.response.OAuthResourceResponse

case class ResourceResponse(underlying: OAuthResourceResponse) {

  def body: String = underlying.getBody

  def contentType: String = underlying.getContentType

  def responseCode: Int = underlying.getResponseCode
  def statusCode: Int   = responseCode

  def param(param: String): Option[String] = Option(underlying.getParam(param))

}
