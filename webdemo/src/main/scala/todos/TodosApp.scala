package todos

import suzaku.app.AppBase
import suzaku.platform.Transport
import suzaku.widget._

class TodosApp(transport: Transport) extends AppBase(transport) {
  override protected def main(): Unit = {
    uiManager.render(Text("Todos"))
  }
}
