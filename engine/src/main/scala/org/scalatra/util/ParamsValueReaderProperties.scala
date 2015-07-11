package org.scalatra.util

import scala.language.implicitConversions

import org.scalatra._

import scala.collection.immutable

trait ParamsValueReaderProperties {

  implicit def stringMapValueReader(d: immutable.Map[String, String]): ValueReader[immutable.Map[String, String], String] =
    new StringMapValueReader(d)

  implicit def multiMapHeadViewMapValueReader[T <: MultiMapHeadView[String, String]](d: T): ValueReader[T, String] =
    new MultiMapHeadViewValueReader(d)

  implicit def multiParamsValueReader(d: MultiParams): ValueReader[MultiParams, Seq[String]] =
    new MultiParamsValueReader(d)

}

object ParamsValueReaderProperties
  extends ParamsValueReaderProperties