package skinny.orm

package object feature {

  @deprecated("Use TimestampsFeatureBase instead.", since = "1.3.4")
  type BaseTimestampsFeature[Entity] = TimestampsFeatureBase[Entity]

}
