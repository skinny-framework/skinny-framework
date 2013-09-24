package skinny.orm.feature.associations

import scala.language.existentials

import skinny.orm._
import skinny.orm.feature._

case class BelongsToExtractor[Entity](
  mapper: AssociationsFeature[_],
  fk: String,
  alias: Alias[_],
  merge: (Entity, Option[Any]) => Entity,
  var byDefault: Boolean = false)

case class HasOneExtractor[Entity](
  mapper: AssociationsFeature[_],
  fk: String,
  alias: Alias[_],
  merge: (Entity, Option[Any]) => Entity,
  var byDefault: Boolean = false)

case class ToManyExtractor[Entity](
  mapper: AssociationsFeature[_],
  alias: Alias[_],
  merge: (Entity, Seq[Any]) => Entity,
  var byDefault: Boolean = false)
