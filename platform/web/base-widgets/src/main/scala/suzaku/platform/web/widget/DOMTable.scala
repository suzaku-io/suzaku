package suzaku.platform.web.widget

import org.scalajs.dom
import org.scalajs.dom.html.TableCell
import suzaku.platform.web.{DOMWidgetArtifact, DOMUIManager, DOMWidgetWithChildren}
import suzaku.ui.WidgetBuilder
import suzaku.widget.{TableBaseProtocol, TableCellProtocol, TableProtocol}

class DOMTable(widgetId: Int, context: TableProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableProtocol.type, dom.html.Table](widgetId, widgetManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.Table]("table"))
}

class DOMTableBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableProtocol) {
  import TableProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTable(widgetId, context, widgetManager)
}

class DOMTableHeader(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableSection](widgetId, widgetManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableSection]("thead"))
}

class DOMTableHeaderBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableHeader(widgetId, context, widgetManager)
}

class DOMTableFooter(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableSection](widgetId, widgetManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableSection]("tfoot"))
}

class DOMTableFooterBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableFooter(widgetId, context, widgetManager)
}

class DOMTableBody(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableSection](widgetId, widgetManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableSection]("tbody"))
}

class DOMTableBodyBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableBody(widgetId, context, widgetManager)
}

class DOMTableRow(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableRow](widgetId, widgetManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableRow]("tr"))
}

class DOMTableRowBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableRow(widgetId, context, widgetManager)
}

class DOMTableCell(widgetId: Int, context: TableCellProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableCellProtocol.type, dom.html.TableCell](widgetId, widgetManager) {

  val artifact = {
    val el = tag[TableCell]("td")
    if (context.colSpan > 1) el.colSpan = context.colSpan
    if (context.rowSpan > 1) el.rowSpan = context.rowSpan
    DOMWidgetArtifact(el)
  }
}

class DOMTableCellBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableCellProtocol) {
  import TableCellProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableCell(widgetId, context, widgetManager)
}

class DOMTableHeaderCell(widgetId: Int, context: TableCellProtocol.ChannelContext, widgetManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableCellProtocol.type, dom.html.TableCell](widgetId, widgetManager) {

  val artifact = {
    val el = tag[TableCell]("th")
    if (context.colSpan > 1) el.colSpan = context.colSpan
    if (context.rowSpan > 1) el.rowSpan = context.rowSpan
    DOMWidgetArtifact(el)
  }
}

class DOMTableHeaderCellBuilder(widgetManager: DOMUIManager) extends WidgetBuilder(TableCellProtocol) {
  import TableCellProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableHeaderCell(widgetId, context, widgetManager)
}
