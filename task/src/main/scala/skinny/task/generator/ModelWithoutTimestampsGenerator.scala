package skinny.task.generator

object ModelWithoutTimestampsGenerator extends ModelGenerator {
  override def withTimestamps: Boolean = false
}
