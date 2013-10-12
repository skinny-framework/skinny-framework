package skinny

/**
 * Skinny Env value (key: "skinny.env")
 */
object SkinnyEnv {

  /**
   * Key for Skinny framework env value.
   */
  val Key = "skinny.env"

  /**
   * Env value from system property (you can pass by JVM options or on runtime) or environment value.
   *
   * @return env string such as "production"
   */
  def get(): Option[String] = Option(System.getProperty(Key)).orElse(Option(System.getenv(Key)))

}
