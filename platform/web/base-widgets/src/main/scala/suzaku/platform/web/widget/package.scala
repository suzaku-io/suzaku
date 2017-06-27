package suzaku.platform.web

import suzaku.platform.web.ui.{DOMGridLayoutBuilder, DOMLinearLayoutBuilder}
import suzaku.ui.WidgetManager
import suzaku.ui.layout.{GridLayout, LinearLayout}
import suzaku.widget._

package object widget {
  def registerWidgets(widgetManager: WidgetManager): Unit = {
    widgetManager.registerWidget(classOf[LinearLayout.WBlueprint], new DOMLinearLayoutBuilder(widgetManager))
    widgetManager.registerWidget(classOf[GridLayout.WBlueprint], new DOMGridLayoutBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Button.WBlueprint], new DOMButtonBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Checkbox.WBlueprint], new DOMCheckboxBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Text.TextBlueprint], new DOMTextBuilder(widgetManager))
    widgetManager.registerWidget(classOf[TextField.WBlueprint], new DOMTextFieldBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.WBlueprint], new DOMTableBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.Header.WBlueprint], new DOMTableHeaderBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.Body.WBlueprint], new DOMTableBodyBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.Footer.WBlueprint], new DOMTableFooterBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.Row.WBlueprint], new DOMTableRowBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.Cell.WBlueprint], new DOMTableCellBuilder(widgetManager))
    widgetManager.registerWidget(classOf[Table.HeaderCell.WBlueprint], new DOMTableHeaderCellBuilder(widgetManager))
  }
}
