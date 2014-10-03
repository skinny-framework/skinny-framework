package module

import model.AppName
import scaldi.Module

class AppModule extends Module {
  bind[AppName] to AppName("config-example")
}
