package skinny

object SkinnyEnv {

  val Key = "skinny.env"

  def get(): Option[String] = Option(System.getenv(Key)).orElse(Option(System.getProperty(Key)))

}
