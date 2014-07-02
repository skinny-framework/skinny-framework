package lib

object AppModule extends scaldi.Module {
  bind[service.EchoService] to service.EchoService()
}
