package service

trait EchoService {
  def echo(s: String): String = s
}

object EchoService {
  def apply(): EchoService = new EchoServiceImpl
}

private[service] class EchoServiceImpl extends EchoService
