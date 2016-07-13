package skinny

/**
 * Model interface for SkinnyResource.
 *
 * @tparam Id id
 * @tparam Model model
 */
trait SkinnyModel[Id, Model] {

  /**
   * Extracts raw value from Identity.
   *
   * @param id  id
   * @return raw value
   */
  def idToRawValue(id: Id): Any

  /**
   * Converts raw value to Identity.
   *
   * @param value raw value
   * @return id
   */
  def rawValueToId(value: Any): Id

  /**
   * Creates new entity with parameters.
   *
   * @param parameters parameters
   * @return generated id
   */
  def createNewModel(parameters: PermittedStrongParameters): Id

  /**
   * Returns the count of all models.
   *
   * @return the count of all models
   */
  def countAllModels(): Long

  /**
   * Returns all models.
   *
   * @return all models
   */
  def findAllModels(): List[Model]

  /**
   * Returns models by paging.
   *
   * @param pageSize page size
   * @param pageNo page no
   * @return models
   */
  def findModels(pageSize: Int, pageNo: Int): List[Model]

  /**
   * Returns models by paging in descending order.
   *
   * @param pageSize page size
   * @param pageNo page no
   * @return models
   */
  def findModelsDesc(pageSize: Int, pageNo: Int): List[Model]

  /**
   * Returns the specified entity if exists.
   * @param id id
   * @return entity if exists
   */
  def findModel(id: Id): Option[Model]

  /**
   * Updates the specified entity with parameters if exists.
   *
   * @param id id
   * @param parameters parameters
   * @return updated count
   */
  def updateModelById(id: Id, parameters: PermittedStrongParameters): Int

  /**
   * Deletes the specified entity if exists.
   * @param id id
   * @return deleted count
   */
  def deleteModelById(id: Id): Int

}
