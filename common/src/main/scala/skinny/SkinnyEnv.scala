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

  /**
   * Predicates current env is "development" or "dev".
   *
   * @return true/false
   */
  def isDevelopment(env: Option[String] = get()): Boolean = env.isEmpty || env.exists(e => e == "development" || e == "dev")

  /**
   * Predicates current env is "test".
   *
   * @return true/false
   */
  def isTest(env: Option[String] = get()): Boolean = env.exists(e => e == "test")

  /**
   * Predicates current env is "staging" or "qa".
   *
   * @return true/false
   */
  def isStaging(env: Option[String] = get()): Boolean = env.exists(e => e == "staging" || e == "qa")

  /**
   * Predicates current env is "production" or "prod".
   *
   * @return true/false
   */
  def isProduction(env: Option[String] = get()): Boolean = env.exists(env => env == "production" || env == "prod")

}
