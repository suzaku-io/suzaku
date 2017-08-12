package todos

import suzaku.app.UIBase
import suzaku.platform.Transport
import suzaku.platform.web.{DOMUIManager, WebPlatform}

class TodosUI(transport: Transport) extends UIBase(transport) {
  override val platform = WebPlatform

  override protected def main(): Unit = {
    suzaku.platform.web.widget.registerWidgets(widgetManager.asInstanceOf[DOMUIManager])
  }
}
