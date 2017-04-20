package webdemo

import suzaku.app.UiBase
import suzaku.platform.Transport
import suzaku.platform.web.widget.{DOMButtonBuilder, DOMListViewBuilder}
import suzaku.widget.Button.ButtonBlueprint
import suzaku.widget.ListView.ListViewBlueprint

class WebDemoUI(transport: Transport) extends UiBase(transport) {
  override val platform = suzaku.platform.PlatformImpl

  override protected def main(): Unit = {
    suzaku.platform.web.widget.registerWidgets(widgetRenderer)
  }
}
