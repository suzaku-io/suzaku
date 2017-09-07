package suzaku.platform.web.widget

import org.scalajs.dom
import org.scalajs.dom.html.TableCell
import suzaku.platform.web.{DOMWidgetArtifact, DOMUIManager, DOMWidgetWithChildren}
import suzaku.ui.WidgetBuilder
import suzaku.widget._

class DOMTable(widgetId: Int, context: TableProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableProtocol.type, dom.html.Table](widgetId, uiManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.Table]("table"))
  DOMTable.init(uiManager)
}

object DOMTable {
  private var initialized = false

  def init(uiManager: DOMUIManager) = {
    if(!initialized) {
      initialized = true
      // initialize CSS required by our tables
      uiManager.addCSS("table", "border-collapse: collapse;")
    }
  }
}

class DOMTableBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableProtocol) {
  import TableProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTable(widgetId, context, uiManager)
}

class DOMTableHeader(widgetId: Int, context: TableHeaderProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableHeaderProtocol.type, dom.html.TableSection](widgetId, uiManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableSection]("thead"))
}

class DOMTableHeaderBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableHeaderProtocol) {
  import TableHeaderProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableHeader(widgetId, context, uiManager)
}

class DOMTableFooter(widgetId: Int, context: TableFooterProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableFooterProtocol.type, dom.html.TableSection](widgetId, uiManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableSection]("tfoot"))
}

class DOMTableFooterBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableFooterProtocol) {
  import TableFooterProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableFooter(widgetId, context, uiManager)
}

class DOMTableBody(widgetId: Int, context: TableBodyProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableBodyProtocol.type, dom.html.TableSection](widgetId, uiManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableSection]("tbody"))
}

class DOMTableBodyBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableBodyProtocol) {
  import TableBodyProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableBody(widgetId, context, uiManager)
}

class DOMTableRow(widgetId: Int, context: TableRowProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableRowProtocol.type, dom.html.TableRow](widgetId, uiManager) {

  val artifact = DOMWidgetArtifact(tag[dom.html.TableRow]("tr"))
}

class DOMTableRowBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableRowProtocol) {
  import TableRowProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableRow(widgetId, context, uiManager)
}

class DOMTableCell(widgetId: Int, context: TableCellProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableCellProtocol.type, dom.html.TableCell](widgetId, uiManager) {

  val artifact = {
    val el = tag[TableCell]("td")
    if (context.colSpan > 1) el.colSpan = context.colSpan
    if (context.rowSpan > 1) el.rowSpan = context.rowSpan
    DOMWidgetArtifact(el)
  }
}

class DOMTableCellBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableCellProtocol) {
  import TableCellProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableCell(widgetId, context, uiManager)
}

class DOMTableHeaderCell(widgetId: Int, context: TableHeaderCellProtocol.ChannelContext, uiManager: DOMUIManager)
    extends DOMWidgetWithChildren[TableHeaderCellProtocol.type, dom.html.TableCell](widgetId, uiManager) {

  val artifact = {
    val el = tag[TableCell]("th")
    if (context.colSpan > 1) el.colSpan = context.colSpan
    if (context.rowSpan > 1) el.rowSpan = context.rowSpan
    DOMWidgetArtifact(el)
  }
}

class DOMTableHeaderCellBuilder(uiManager: DOMUIManager) extends WidgetBuilder(TableHeaderCellProtocol) {
  import TableHeaderCellProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableHeaderCell(widgetId, context, uiManager)
}
