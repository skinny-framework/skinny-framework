package skinny

/**
 * Strong parameters which is inspired by Rails4's mass assignment protection.
 *
 * @param params params
 */
case class StrongParameters(params: Map[String, Any]) {
  def permit(nameAndTypes: (String, ParamType)*): PermittedStrongParameters = {
    new PermittedStrongParameters(
      params.filter { case (name, _) => nameAndTypes.exists(_._1 == name) }.map {
        case (name, value) =>
          name -> (value -> nameAndTypes.find(_._1 == name).get._2)
      }
    )
  }
}

/**
 * Permitted strong parameters.
 *
 * @param params params
 */
class PermittedStrongParameters(val params: Map[String, (Any, ParamType)])
