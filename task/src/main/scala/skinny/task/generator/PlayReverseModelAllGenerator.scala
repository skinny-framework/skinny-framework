package skinny.task.generator

/**
  * Reverse model all generator for Play Framework users.
  */
trait PlayReverseModelAllGenerator extends ReverseModelAllGenerator with PlayConfiguration {
  override def sourceDir                               = "app"
  override def testSourceDir                           = "test"
  override def modelPackage                            = "models"
  override def initializeDB(skinnyEnv: Option[String]) = playDBs.setupAll()
}

object PlayReverseModelAllGenerator extends PlayReverseModelAllGenerator
