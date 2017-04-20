package suzaku.platform.web

import suzaku.ui.WidgetRenderer
import suzaku.widget.Button.ButtonBlueprint
import suzaku.widget.ListView.ListViewBlueprint
import suzaku.widget.Text.TextBlueprint

package object widget {
  def registerWidgets(registry: WidgetRenderer): Unit = {
    registry.registerWidget(classOf[ButtonBlueprint], DOMButtonBuilder)
    registry.registerWidget(classOf[ListViewBlueprint], DOMListViewBuilder)
    registry.registerWidget(classOf[TextBlueprint], DOMTextBuilder)
  }
}
