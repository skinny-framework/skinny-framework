package lib

object AppModule extends AppModule

class AppModule extends scaldi.Module {
  bind[service.EchoService] to service.EchoService()
}
