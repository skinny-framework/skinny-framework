package skinny.orm.feature.associations

sealed trait JoinType
case object InnerJoin extends JoinType
case object LeftOuterJoin extends JoinType
