package skinny.oauth2.client

import org.apache.oltu.oauth2.common.message.types.{ ResponseType => OltuResponseType }

/**
  * Response Type.
  */
case class ResponseType(value: String)

object ResponseType {

  def apply(enum: OltuResponseType): ResponseType = new ResponseType(enum.toString)

  val Code  = ResponseType(OltuResponseType.CODE)
  val Token = ResponseType(OltuResponseType.TOKEN)

}
