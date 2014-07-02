package module

import scaldi.Module
import service._

class ServicesModule extends Module {
  bind[EchoService] to new EchoServiceUpperCaseImpl
}
