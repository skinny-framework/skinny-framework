package module

class TestModule extends scaldi.Module {
  bind[service.EchoService] to new service.EchoServiceTestImpl
}

