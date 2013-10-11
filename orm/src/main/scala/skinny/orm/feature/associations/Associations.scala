package skinny.orm.feature.associations

import scala.language.existentials

import skinny.orm.feature._
import scala.collection.mutable

sealed trait Association[Entity] {
  def underlying: AssociationsFeature[Entity]
}

case class BelongsToAssociation[Entity](
  underlying: AssociationsFeature[Entity],
  joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]],
  extractor: BelongsToExtractor[Entity])
    extends Association[Entity] {

  underlying.associations.add(this)

  def byDefault(): BelongsToAssociation[Entity] = {
    joinDefinitions.foreach { joinDef =>
      joinDef.byDefault(joinDef.enabledEvenIfAssociated)
      underlying.defaultJoinDefinitions.add(joinDef)
    }
    underlying.setAsByDefault(extractor)
    this
  }
}

case class HasOneAssociation[Entity](
  underlying: AssociationsFeature[Entity],
  joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]],
  extractor: HasOneExtractor[Entity])
    extends Association[Entity] {

  underlying.associations.add(this)

  def byDefault(): HasOneAssociation[Entity] = {
    joinDefinitions.foreach { joinDef =>
      joinDef.byDefault(joinDef.enabledEvenIfAssociated)
      underlying.defaultJoinDefinitions.add(joinDef)
    }
    underlying.setAsByDefault(extractor)
    this
  }
}

case class HasManyAssociation[Entity](
  underlying: AssociationsFeature[Entity],
  joinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]],
  extractor: ToManyExtractor[Entity])
    extends Association[Entity] {

  underlying.associations.add(this)

  def byDefault(): HasManyAssociation[Entity] = {
    joinDefinitions.foreach { joinDef =>
      joinDef.byDefault(joinDef.enabledEvenIfAssociated)
      underlying.defaultJoinDefinitions.add(joinDef)
    }
    underlying.setAsByDefault(extractor)
    this
  }
}
