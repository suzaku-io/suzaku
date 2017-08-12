package webdemo

import suzaku.app.UIBase
import suzaku.platform.Transport
import suzaku.platform.web.{DOMUIManager, WebPlatform}

class WebDemoUI(transport: Transport) extends UIBase(transport) {
  override val platform = WebPlatform

  override protected def main(): Unit = {
    suzaku.platform.web.widget.registerWidgets(widgetManager.asInstanceOf[DOMUIManager])
  }
}
