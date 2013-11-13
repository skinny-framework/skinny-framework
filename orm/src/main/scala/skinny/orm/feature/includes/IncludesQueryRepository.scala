package skinny.orm.feature.includes

import skinny.orm.feature.associations._
import scala.collection.mutable

trait IncludesQueryRepository[Entity] {

  private[this] val belongsTo: mutable.Map[BelongsToExtractor[Entity], Seq[_]] = new mutable.HashMap()
  private[this] val hasOne: mutable.Map[HasOneExtractor[Entity], Seq[_]] = new mutable.HashMap()
  private[this] val hasMany: mutable.Map[HasManyExtractor[Entity], Seq[_]] = new mutable.HashMap()

  def entitiesFor(extractor: BelongsToExtractor[Entity]): Seq[_] = belongsTo.getOrElse(extractor, Nil)
  def entitiesFor(extractor: HasOneExtractor[Entity]): Seq[_] = hasOne.getOrElse(extractor, Nil)
  def entitiesFor(extractor: HasManyExtractor[Entity]): Seq[_] = hasMany.getOrElse(extractor, Nil)

  def put(extractor: BelongsToExtractor[Entity], entity: Any) = {
    belongsTo.update(extractor, belongsTo.getOrElse(extractor, Nil).+:(entity))
  }
  def put(extractor: HasOneExtractor[Entity], entity: Any) = {
    hasOne.update(extractor, hasOne.getOrElse(extractor, Nil).+:(entity))
  }
  def put(extractor: HasManyExtractor[Entity], entity: Any) = {
    hasMany.update(extractor, hasMany.getOrElse(extractor, Nil).+:(entity))
  }
}

object IncludesQueryRepository {

  def apply[Entity](): IncludesQueryRepository[Entity] = new DefaultIncludesQueryRepository[Entity]
}

class DefaultIncludesQueryRepository[Entity] extends IncludesQueryRepository[Entity]