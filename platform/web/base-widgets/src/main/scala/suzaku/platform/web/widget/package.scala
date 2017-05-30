package suzaku.platform.web

import suzaku.platform.web.ui.DOMLinearLayoutBuilder
import suzaku.ui.{LinearLayout, WidgetRenderer}
import suzaku.widget._

package object widget {
  def registerWidgets(registry: WidgetRenderer): Unit = {
    registry.registerWidget(classOf[LinearLayout.WBlueprint], DOMLinearLayoutBuilder)
    registry.registerWidget(classOf[Button.WBlueprint], DOMButtonBuilder)
    registry.registerWidget(classOf[ListView.WBlueprint], DOMListViewBuilder)
    registry.registerWidget(classOf[Text.TextBlueprint], DOMTextBuilder)
    registry.registerWidget(classOf[TextInput.WBlueprint], DOMTextInputBuilder)
  }
}
