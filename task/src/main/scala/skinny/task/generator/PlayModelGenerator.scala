package skinny.task.generator

/**
 * Model generator for Play Framework users.
 */
trait PlayModelGenerator extends ModelGenerator {
  override def useAutoConstruct = true
  override def sourceDir = "app"
  override def testSourceDir = "test"
  override def modelPackage = "models"
}

object PlayModelGenerator extends PlayModelGenerator
