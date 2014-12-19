package skinny.task.generator

/**
 * Reverse model generator for Play Framework users.
 */
trait PlayReverseModelGenerator extends ReverseModelGenerator with PlayConfiguration {
  override def useAutoConstruct = true
  override def sourceDir = "app"
  override def testSourceDir = "test"
  override def modelPackage = "models"
  override def initializeDB(skinnyEnv: Option[String]) = playDBs.setupAll()
}

object PlayReverseModelGenerator extends PlayReverseModelGenerator