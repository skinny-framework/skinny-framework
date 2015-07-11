package org.scalatra.util

trait ValueReader[S, U] {

  def data: S

  def read(key: String): Either[String, Option[U]]

}
