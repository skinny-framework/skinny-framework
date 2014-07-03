package service

class EchoServiceTestImpl extends EchoService {

  override def echo(s: String): String = s.toUpperCase(java.util.Locale.ENGLISH)
}
