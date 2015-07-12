package skinny.engine.test

abstract class Uploadable {

  def content: Array[Byte]

  def fileName: String

  def contentType: String

  def contentLength: Long

}
