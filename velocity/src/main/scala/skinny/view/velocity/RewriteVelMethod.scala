package skinny.view.velocity

import java.lang.reflect.Method
import org.apache.velocity.util.introspection.VelMethod

/**
  * Rewrite of access to method
  *
  * @param method original method
  * @param fun function to be executed instead of the original method
  */
class RewriteVelMethod(method: Method, fun: (AnyRef, Array[AnyRef]) => AnyRef) extends VelMethod {

  override def getMethodName: String = method.getName

  override def getReturnType: Class[_] = method.getReturnType

  override def isCacheable: Boolean = true

  override def invoke(o: AnyRef, params: Array[AnyRef]): AnyRef = fun(o, params)

}
