package skinny.orm

/**
 * Basic SkinnyMapper implementation.
 *
 * @tparam Entity entity
 */
trait SkinnyMapper[Entity] extends SkinnyMapperWithId[Long, Entity] {
  override def rawValueToId(value: Any) = value.toString.toLong
  override def idToRawValue(id: Long) = id
}
