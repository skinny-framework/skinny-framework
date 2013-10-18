package skinny

/**
 * Model interface for SkinnyResource.
 *
 * @tparam Entity entity
 */
trait SkinnyModel[Entity] {

  /**
   * Creates new entity with parameters.
   *
   * @param parameters parameters
   * @return generated id
   */
  def createNewModel(parameters: PermittedStrongParameters): Long

  /**
   * Returns all entities.
   *
   * @return all entities
   */
  def findAllModels(): List[Entity]

  /**
   * Returns the specified entity if exists.
   * @param id id
   * @return entity if exists
   */
  def findModel(id: Long): Option[Entity]

  /**
   * Updates the specified entity with parameters if exists.
   *
   * @param id id
   * @param parameters parameters
   * @return updated count
   */
  def updateModelById(id: Long, parameters: PermittedStrongParameters): Int

  /**
   * Deletes the specified entity if exists.
   * @param id id
   * @return deleted count
   */
  def deleteModelById(id: Long): Int

}
