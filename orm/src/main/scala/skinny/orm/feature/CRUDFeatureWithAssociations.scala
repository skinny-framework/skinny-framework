package skinny.orm.feature

/**
 * Extended CRUDFeature which supports associations.
 */
trait CRUDFeatureWithAssociations[Entity]
  extends JoinsFeature[Entity]
  with CRUDFeature[Entity]