package skinny.http.test

case class HttpMethod(name: String)

object HttpMethod {
  val GET = HttpMethod("GET")
  val HEAD = HttpMethod("HEAD")
  val POST = HttpMethod("POST")
  val PUT = HttpMethod("PUT")
  val DELETE = HttpMethod("DELETE")
  val OPTIONS = HttpMethod("OPTIONS")
  val TRACE = HttpMethod("TRACE")
}
