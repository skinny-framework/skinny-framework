package skinny.engine.data

import scala.language.implicitConversions

import skinny.engine.MultiParams

/**
 * Properties for params value reader.
 */
trait ParamsValueReaderProperties {

  implicit def stringMapValueReader(d: Map[String, String]): ValueReader[Map[String, String], String] =
    new StringMapValueReader(d)

  implicit def multiMapHeadViewMapValueReader[T <: MultiMapHeadView[String, String]](d: T): ValueReader[T, String] =
    new MultiMapHeadViewValueReader(d)

  implicit def multiParamsValueReader(d: MultiParams): ValueReader[MultiParams, Seq[String]] =
    new MultiParamsValueReader(d)

}

object ParamsValueReaderProperties
  extends ParamsValueReaderProperties