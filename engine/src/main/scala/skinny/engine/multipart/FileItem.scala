package skinny.engine.multipart

import java.io.{ File, FileOutputStream, InputStream }
import javax.servlet.http._

import skinny.util.LoanPattern._
import skinny.engine.implicits.RicherStringImplicits
import skinny.engine.util.io

/**
 * File item.
 * @param part file part.
 */
case class FileItem(part: Part) {

  import RicherStringImplicits._

  val size: Long = part.getSize
  val fieldName: String = part.getName
  val name: String = partAttribute(part, "content-disposition", "filename")
  val contentType: Option[String] = part.getContentType.blankOption
  val charset: Option[String] = partAttribute(part, "content-type", "charset").blankOption

  def getName: String = name

  def getFieldName: String = fieldName

  def getSize: Long = size

  def getContentType: Option[String] = contentType.orElse(null)

  def getCharset: Option[String] = charset.orElse(null)

  def write(file: File): Unit = {
    using(new FileOutputStream(file)) { out =>
      io.copy(getInputStream, out)
    }
  }

  def write(fileName: String): Unit = {
    part.write(fileName)
  }

  def get(): Array[Byte] = io.readBytes(getInputStream)

  def isFormField: Boolean = (name == null)

  def getInputStream: InputStream = part.getInputStream

  private[this] def partAttribute(
    part: Part,
    headerName: String, attributeName: String,
    defaultValue: String = null): String = {

    Option(part.getHeader(headerName)) match {
      case Some(value) => {
        value.split(";").find(_.trim().startsWith(attributeName)) match {
          case Some(attributeValue) => attributeValue.substring(attributeValue.indexOf('=') + 1).trim().replace("\"", "")
          case _ => defaultValue
        }
      }
      case _ => defaultValue
    }
  }

}
