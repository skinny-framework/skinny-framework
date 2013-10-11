package skinny.util

object JavaReflectAPI {

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
