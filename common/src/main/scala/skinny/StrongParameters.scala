package skinny

/**
 * Strong parameters which is inspired by Rails4's mass assignment protection.
 *
 * @param params params
 */
case class StrongParameters(params: Map[String, Any]) {

  /**
   * Permits parameters to be updated.
   *
   * @param paramKeyAndParamTypes name and param type
   * @return permitted parameters
   */
  def permit(paramKeyAndParamTypes: (String, ParamType)*): PermittedStrongParameters = {
    val _params = params
      .filter { case (name, _) => paramKeyAndParamTypes.exists(_._1 == name) }
      .flatMap {
        case (name, value) =>
          paramKeyAndParamTypes.find(_._1 == name).map {
            case (_, ParamType.Boolean) => name -> (Option(value).getOrElse(false) -> ParamType.Boolean)
            case (_, paramType) => name -> (value -> paramType)
          }
      }
    val nullableBooleanParams = paramKeyAndParamTypes
      .filter(_._2 == ParamType.Boolean)
      .filterNot { case (paramKey, _) => params.keys.exists(_ == paramKey) }
      .map { case (name, _) => name -> (false, ParamType.Boolean) }

    new PermittedStrongParameters(_params ++ nullableBooleanParams)
  }
}

/**
 * Permitted strong parameters.
 *
 * @param params params
 */
class PermittedStrongParameters(val params: Map[String, (Any, ParamType)])
