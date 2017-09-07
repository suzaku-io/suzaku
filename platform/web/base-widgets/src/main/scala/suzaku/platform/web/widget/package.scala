package suzaku.platform.web

import suzaku.platform.web.ui.{DOMGridLayoutBuilder, DOMLinearLayoutBuilder}
import suzaku.ui.layout.{GridLayoutProtocol, LinearLayoutProtocol}
import suzaku.widget._

package object widget {
  def registerWidgets(widgetManager: DOMUIManager): Unit = {
    widgetManager.registerWidget(LinearLayoutProtocol.widgetName, new DOMLinearLayoutBuilder(widgetManager))
    widgetManager.registerWidget(GridLayoutProtocol.widgetName, new DOMGridLayoutBuilder(widgetManager))
    widgetManager.registerWidget(ButtonProtocol.widgetName, new DOMButtonBuilder(widgetManager))
    widgetManager.registerWidget(CheckboxProtocol.widgetName, new DOMCheckboxBuilder(widgetManager))
    widgetManager.registerWidget(TextProtocol.widgetName, new DOMTextBuilder(widgetManager))
    widgetManager.registerWidget(TextFieldProtocol.widgetName, new DOMTextFieldBuilder(widgetManager))
    widgetManager.registerWidget(TableProtocol.widgetName, new DOMTableBuilder(widgetManager))
    widgetManager.registerWidget(TableHeaderProtocol.widgetName, new DOMTableHeaderBuilder(widgetManager))
    widgetManager.registerWidget(TableBodyProtocol.widgetName, new DOMTableBodyBuilder(widgetManager))
    widgetManager.registerWidget(TableFooterProtocol.widgetName, new DOMTableFooterBuilder(widgetManager))
    widgetManager.registerWidget(TableRowProtocol.widgetName, new DOMTableRowBuilder(widgetManager))
    widgetManager.registerWidget(TableCellProtocol.widgetName, new DOMTableCellBuilder(widgetManager))
    widgetManager.registerWidget(TableHeaderCellProtocol.widgetName, new DOMTableHeaderCellBuilder(widgetManager))
  }
}
