package service

trait EchoService {
  def echo(msg: String): String
}

class EchoServiceImpl extends EchoService {
  override def echo(msg: String) = msg
}
class EchoServiceUpperCaseImpl extends EchoService {
  override def echo(msg: String) = msg.toUpperCase(java.util.Locale.ENGLISH)
}
