package suzaku.platform.web

import suzaku.platform.web.ui.DOMLinearLayoutBuilder
import suzaku.ui.WidgetManager
import suzaku.ui.layout.LinearLayout
import suzaku.widget._

package object widget {
  def registerWidgets(widgetManager: WidgetManager): Unit = {
    widgetManager.registerWidget(classOf[LinearLayout.WBlueprint], new DOMLinearLayoutBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Button.WBlueprint], new DOMButtonBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Checkbox.WBlueprint], new DOMCheckboxBuilder(widgetManager))
    widgetManager.registerWidget(classOf[ListView.WBlueprint], new DOMListViewBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Text.TextBlueprint], new DOMTextBuilder(widgetManager))
    widgetManager.registerWidget(classOf[TextInput.WBlueprint], new DOMTextInputBuilder(widgetManager))
  }
}
