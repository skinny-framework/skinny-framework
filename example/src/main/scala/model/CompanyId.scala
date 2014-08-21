package model

import scalikejdbc.TypeBinder

case class CompanyId(value: Long) {
  override def toString = value.toString
}

object CompanyId {
  implicit val companyIdTypeBinder: TypeBinder[CompanyId] = TypeBinder.long.map(CompanyId.apply)
}