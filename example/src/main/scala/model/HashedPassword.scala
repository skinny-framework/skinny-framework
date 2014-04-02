package model

import scalikejdbc.TypeBinder

case class HashedPassword private[model] (value: String) {
  def verify(plainPassword: PlainPassword, salt: Any): Boolean = plainPassword.hash(salt) == this
}
object HashedPassword {
  implicit val typeBinder: TypeBinder[HashedPassword] = TypeBinder.string.map(HashedPassword.apply)
}
