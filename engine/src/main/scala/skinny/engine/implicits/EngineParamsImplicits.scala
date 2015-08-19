package skinny.engine.implicits

import scala.language.implicitConversions

import java.util.Date

import skinny.engine.{ MultiParams, Params, SkinnyEngineException }

/**
 * Implicit conversion for EngineParams.
 */
trait EngineParamsImplicits {

  self: DefaultImplicits =>

  sealed class TypedParams(params: Params) {

    def getAs[T <: Any](name: String)(implicit tc: TypeConverter[String, T]): Option[T] = params.get(name).flatMap(tc(_))

    def getAs[T <: Date](nameAndFormat: (String, String)): Option[Date] = getAs(nameAndFormat._1)(stringToDate(nameAndFormat._2))

    def as[T <: Any](name: String)(implicit tc: TypeConverter[String, T]): T =
      getAs[T](name) getOrElse (throw new SkinnyEngineException("Key %s could not be found.".format(name)))

    def as[T <: Date](nameAndFormat: (String, String)): Date =
      getAs[T](nameAndFormat) getOrElse (throw new SkinnyEngineException("Key %s could not be found.".format(nameAndFormat._1)))

    def getAsOrElse[T <: Any](name: String, default: => T)(implicit tc: TypeConverter[String, T]): T =
      getAs[T](name).getOrElse(default)

    def getAsOrElse(nameAndFormat: (String, String), default: => Date)(implicit tc: TypeConverter[String, Date]): Date =
      getAs[Date](nameAndFormat).getOrElse(default)

  }

  sealed class TypedMultiParams(multiParams: MultiParams) {

    def getAs[T <: Any](name: String)(implicit tc: TypeConverter[String, T]): Option[Seq[T]] = {
      multiParams.get(name) map { s => s.flatMap(tc.apply(_)) }
    }

    def getAs[T <: Date](nameAndFormat: (String, String)): Option[Seq[Date]] = {
      getAs(nameAndFormat._1)(stringToDate(nameAndFormat._2))
    }

    def as[T <: Any](name: String)(implicit tc: TypeConverter[String, T]): Seq[T] =
      getAs[T](name) getOrElse (throw new SkinnyEngineException("Key %s could not be found.".format(name)))

    def as[T <: Date](nameAndFormat: (String, String)): Seq[Date] =
      getAs[T](nameAndFormat) getOrElse (throw new SkinnyEngineException("Key %s could not be found.".format(nameAndFormat._1)))

    def getAsOrElse[T <: Any](name: String, default: => Seq[T])(implicit tc: TypeConverter[String, T]): Seq[T] =
      getAs[T](name).getOrElse(default)

    def getAsOrElse(nameAndFormat: (String, String), default: => Seq[Date])(
      implicit tc: TypeConverter[String, Date]): Seq[Date] = {
      getAs[Date](nameAndFormat).getOrElse(default)
    }

  }

  implicit def toTypedParams(params: Params): TypedParams = new TypedParams(params)

  implicit def toTypedMultiParams(params: MultiParams): TypedMultiParams = new TypedMultiParams(params)

}

object EngineParamsImplicits
  extends EngineParamsImplicits
  with DefaultImplicits
