package suzaku.platform.web.widget

import org.scalajs.dom
import suzaku.platform.web.{DOMWidget, DOMWidgetArtifact, DOMWidgetWithChildren}
import suzaku.ui.{Widget, WidgetBuilder, WidgetManager}
import suzaku.widget.{TableBaseProtocol, TableProtocol}

class DOMTable(widgetId: Int, context: TableProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidgetWithChildren[TableProtocol.type, dom.html.Table](widgetId, widgetManager) {
  import TableProtocol._

  val artifact = {
    val el = tag[dom.html.Table]("table")
    DOMWidgetArtifact(el)
  }

}

class DOMTableBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TableProtocol) {
  import TableProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTable(widgetId, context, widgetManager)
}

class DOMTableHeader(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableSection](widgetId, widgetManager) {
  import TableBaseProtocol._

  val artifact = {
    val el = tag[dom.html.TableSection]("thead")
    DOMWidgetArtifact(el)
  }

  override def setChildren(children: Seq[Widget]) = {
    import org.scalajs.dom.ext._
    modifyDOM { el =>
      el.childNodes.foreach(el.removeChild)
      children.foreach { c =>
        val widget = c.asInstanceOf[DOMWidget[_, _ <: dom.html.Element]]
        val th     = tag[dom.html.TableHeaderCell]("th")
        th.appendChild(widget.artifact.el)
        el.appendChild(th)
        resolveLayout(widget, widget.layoutProperties)
      }
    }
  }
}

class DOMTableHeaderBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableHeader(widgetId, context, widgetManager)
}

class DOMTableBody(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableSection](widgetId, widgetManager) {
  import TableBaseProtocol._

  val artifact = {
    val el = tag[dom.html.TableSection]("tbody")
    DOMWidgetArtifact(el)
  }
}

class DOMTableBodyBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableBody(widgetId, context, widgetManager)
}

class DOMTableFooter(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableSection](widgetId, widgetManager) {
  import TableBaseProtocol._

  val artifact = {
    val el = tag[dom.html.TableSection]("tfooter")
    DOMWidgetArtifact(el)
  }
}

class DOMTableFooterBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableFooter(widgetId, context, widgetManager)
}

class DOMTableRow(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableRow](widgetId, widgetManager) {
  import TableBaseProtocol._

  val artifact = {
    val el = tag[dom.html.TableRow]("tr")
    DOMWidgetArtifact(el)
  }
}

class DOMTableRowBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableRow(widgetId, context, widgetManager)
}

class DOMTableCell(widgetId: Int, context: TableBaseProtocol.ChannelContext, widgetManager: WidgetManager)
    extends DOMWidgetWithChildren[TableBaseProtocol.type, dom.html.TableCell](widgetId, widgetManager) {
  import TableBaseProtocol._

  val artifact = {
    val el = tag[dom.html.TableCell]("td")
    DOMWidgetArtifact(el)
  }
}

class DOMTableCellBuilder(widgetManager: WidgetManager) extends WidgetBuilder(TableBaseProtocol) {
  import TableBaseProtocol._

  override protected def create(widgetId: Int, context: ChannelContext) =
    new DOMTableCell(widgetId, context, widgetManager)
}
