package skinny.engine.data

import skinny.engine.MultiParams

import scala.collection.immutable
import scala.util.control.Exception.allCatch

class StringMapValueReader(val data: Map[String, String])
    extends ValueReader[immutable.Map[String, String], String] {

  def read(key: String): Either[String, Option[String]] =
    allCatch.withApply(t => Left(t.getMessage)) { Right(data get key) }

}

class MultiMapHeadViewValueReader[T <: MultiMapHeadView[String, String]](val data: T)
    extends ValueReader[T, String] {

  def read(key: String): Either[String, Option[String]] =
    allCatch.withApply(t => Left(t.getMessage)) { Right(data get key) }

}

class MultiParamsValueReader(val data: MultiParams)
    extends ValueReader[MultiParams, Seq[String]] {

  def read(key: String): Either[String, Option[Seq[String]]] =
    allCatch.withApply(t => Left(t.getMessage)) { Right(data get key) }
}
