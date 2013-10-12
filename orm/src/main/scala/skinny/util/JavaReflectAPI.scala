package skinny.util

/**
 * Java reflection API utils.
 */
object JavaReflectAPI {

  /**
   * Returns the simple name of the object's class even if invoked on the Scala REPL.
   *
   * @param obj target object
   * @return simple class name
   */
  def getSimpleName(obj: Any): String = {
    try obj.getClass.getSimpleName
    catch {
      case e: InternalError =>
        // working on the Scala REPL
        val clazz = obj.getClass
        val classOfClazz = clazz.getClass
        val getSimpleBinaryName = classOfClazz.getDeclaredMethods.find(_.getName == "getSimpleBinaryName").get
        getSimpleBinaryName.setAccessible(true)
        getSimpleBinaryName.invoke(clazz).toString
    }
  }

}
