package model

import skinny.{ TypeConverter, TypeConverterSupport }

case class PlainPassword(value: String) {
  def hash(salt: Any): HashedPassword = {
    // attention!: this simple SHA-1 hash is not secure. this is used only in test.
    val md = java.security.MessageDigest.getInstance("SHA-1")
    HashedPassword(md.digest((value + salt).getBytes("UTF-8")).map("%02x".format(_)).mkString)
  }
}
object PlainPassword {
  implicit val typeConverter: TypeConverter[String, PlainPassword] = TypeConverterSupport.safe(PlainPassword.apply)
}
