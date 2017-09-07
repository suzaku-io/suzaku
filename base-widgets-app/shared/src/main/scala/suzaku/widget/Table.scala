package suzaku.widget

import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object Table extends WidgetProtocolProvider {

  class WProxy private[Table] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableProtocol, bd, widgetId, uiChannel) {
    import TableProtocol._

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[Table] ()(body: Body.WBlueprint,
                                          header: Option[Header.WBlueprint],
                                          footer: Option[Footer.WBlueprint])
      extends WidgetBlueprint {
    type P     = TableProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = (header :: footer :: Some(body) :: Nil).flatten

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)

    def apply(footer: Footer.WBlueprint): WBlueprint = {
      this.copy()(body, header, Some(footer))
    }

    def footer(footer: Footer.WBlueprint): WBlueprint = {
      this.copy()(body, header, Some(footer))
    }

    def footer(colName: String, colNames: String*): WBlueprint = {
      this.copy()(body, header, Some(Footer(colName, colNames: _*)))
    }
  }

  override def widgetProtocol = TableProtocol

  class TableBuilder {
    def apply(header: Header.WBlueprint): TableBuilderHeader =
      new TableBuilderHeader(header)
    def header(header: Header.WBlueprint): TableBuilderHeader =
      new TableBuilderHeader(header)
    def header(colName: String, colNames: String*) =
      new TableBuilderHeader(Header(colName, colNames: _*))

    def apply(body: Body.WBlueprint): WBlueprint =
      WBlueprint()(body, None, None)
    def body(body: Body.WBlueprint): WBlueprint =
      WBlueprint()(body, None, None)

    def apply(row: Row.WBlueprint, rows: Row.WBlueprint*): WBlueprint =
      WBlueprint()(Body(row, rows: _*), None, None)
    def body(row: Row.WBlueprint, rows: Row.WBlueprint*): WBlueprint =
      WBlueprint()(Body(row, rows: _*), None, None)
  }

  class TableBuilderHeader(header: Header.WBlueprint) {
    def apply(body: Body.WBlueprint): WBlueprint =
      WBlueprint()(body, Some(header), None)

    def body(body: Body.WBlueprint): WBlueprint =
      WBlueprint()(body, Some(header), None)

    def apply(row: Row.WBlueprint, rows: Row.WBlueprint*): WBlueprint =
      WBlueprint()(Body(row, rows: _*), Some(header), None)

    def body(row: Row.WBlueprint, rows: Row.WBlueprint*): WBlueprint =
      WBlueprint()(Body(row, rows: _*), Some(header), None)

    def apply(rows: Seq[Row.WBlueprint]): WBlueprint =
      WBlueprint()(Body(rows), Some(header), None)

    def body(rows: Seq[Row.WBlueprint]): WBlueprint =
      WBlueprint()(Body(rows), Some(header), None)
  }

  def apply(): TableBuilder                                = new TableBuilder
  def apply(header: Header.WBlueprint): TableBuilderHeader = new TableBuilderHeader(header)
  def apply(body: Body.WBlueprint): WBlueprint             = WBlueprint()(body, None, None)

  sealed trait TableCellBlueprint extends WidgetBlueprint

  object Header extends WidgetProtocolProvider {

    class WProxy private[Header] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
        extends WidgetProxy(TableHeaderProtocol, bd, widgetId, uiChannel) {
      import TableHeaderProtocol._

      override protected def initWidget = ChannelContext()

      override def update(newBlueprint: WBlueprint) = {
        super.update(newBlueprint)
      }
    }

    case class WBlueprint private[Header] ()(rows: List[Row.WBlueprint]) extends WidgetBlueprint {
      type P     = TableHeaderProtocol.type
      type Proxy = WProxy
      type This  = WBlueprint

      override def children = rows

      override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
    }

    override def widgetProtocol = TableHeaderProtocol

    def apply(colName: String, colNames: String*) =
      WBlueprint()(Row(HeaderCell(Text(colName)), colNames.map(name => HeaderCell(Text(name))): _*) :: Nil)

    def apply(col: TableCellBlueprint, cols: TableCellBlueprint*) = WBlueprint()(Row(col, cols: _*) :: Nil)

    def apply(col: Blueprint, cols: Blueprint*) = WBlueprint()(Row(HeaderCell(col), cols.map(HeaderCell(_)): _*) :: Nil)

    def apply(row: Row.WBlueprint, rows: Row.WBlueprint*) = WBlueprint()(row :: rows.toList)
  }

  object Footer extends WidgetProtocolProvider {

    class WProxy private[Footer] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
        extends WidgetProxy(TableFooterProtocol, bd, widgetId, uiChannel) {
      import TableFooterProtocol._

      override protected def initWidget = ChannelContext()

      override def update(newBlueprint: WBlueprint) = {
        super.update(newBlueprint)
      }
    }

    case class WBlueprint private[Footer] ()(rows: List[Row.WBlueprint]) extends WidgetBlueprint {
      type P     = TableFooterProtocol.type
      type Proxy = WProxy
      type This  = WBlueprint

      override def children = rows

      override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
    }

    override def widgetProtocol = TableFooterProtocol

    def apply(colName: String, colNames: String*) =
      WBlueprint()(Row(Cell(Text(colName)), colNames.map(name => Cell(Text(name))): _*) :: Nil)

    def apply(col: TableCellBlueprint, cols: TableCellBlueprint*) = WBlueprint()(Row(col, cols: _*) :: Nil)

    def apply(row: Row.WBlueprint, rows: Row.WBlueprint*) = WBlueprint()(row :: rows.toList)
  }

  object Body extends WidgetProtocolProvider {

    class WProxy private[Body] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
        extends WidgetProxy(TableBodyProtocol, bd, widgetId, uiChannel) {
      import TableBodyProtocol._

      override protected def initWidget = ChannelContext()

      override def update(newBlueprint: WBlueprint) = {
        super.update(newBlueprint)
      }
    }

    case class WBlueprint private[Body] ()(rows: Seq[Row.WBlueprint]) extends WidgetBlueprint {
      type P     = TableBodyProtocol.type
      type Proxy = WProxy
      type This  = WBlueprint

      override def children = rows

      override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
    }

    override def widgetProtocol = TableBodyProtocol

    def apply(row: Row.WBlueprint, rows: Row.WBlueprint*): WBlueprint = WBlueprint()(row :: rows.toList)

    def apply(rows: Seq[Row.WBlueprint]): WBlueprint = WBlueprint()(rows.toList)
  }

  object Row extends WidgetProtocolProvider {

    class WProxy private[Row] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
        extends WidgetProxy(TableRowProtocol, bd, widgetId, uiChannel) {
      import TableRowProtocol._

      override protected def initWidget = ChannelContext()

      override def update(newBlueprint: WBlueprint) = {
        super.update(newBlueprint)
      }
    }

    case class WBlueprint private[Row] ()(cells: List[TableCellBlueprint]) extends WidgetBlueprint {
      type P     = TableRowProtocol.type
      type Proxy = WProxy
      type This  = WBlueprint

      override def children = cells

      override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
    }

    override def widgetProtocol = TableRowProtocol

    def apply(cell: TableCellBlueprint, cells: TableCellBlueprint*) = WBlueprint()(cell :: cells.toList)
  }

  object Cell extends WidgetProtocolProvider {

    class WProxy private[Cell] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
        extends WidgetProxy(TableCellProtocol, bd, widgetId, uiChannel) {
      import TableCellProtocol._

      override protected def initWidget = ChannelContext(bd.colSpan, bd.rowSpan)

      override def update(newBlueprint: WBlueprint) = {
        if (bd.colSpan != newBlueprint.colSpan || bd.rowSpan != newBlueprint.rowSpan)
          send(SetSpans(newBlueprint.colSpan, newBlueprint.rowSpan))
        super.update(newBlueprint)
      }
    }

    case class WBlueprint private[Cell] (colSpan: Int, rowSpan: Int)(content: List[Blueprint]) extends TableCellBlueprint {
      type P     = TableCellProtocol.type
      type Proxy = WProxy
      type This  = WBlueprint

      override def children = content

      override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)

      def colSpan(span: Int): This = WBlueprint(span, rowSpan)(content)

      def rowSpan(span: Int): This = WBlueprint(colSpan, span)(content)
    }

    override def widgetProtocol = TableCellProtocol

    def apply(content: Blueprint*) = WBlueprint(1, 1)(content.toList)

  }

  object HeaderCell extends WidgetProtocolProvider {

    class WProxy private[HeaderCell] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
        extends WidgetProxy(TableHeaderCellProtocol, bd, widgetId, uiChannel) {
      import TableHeaderCellProtocol._

      override protected def initWidget = ChannelContext(bd.colSpan, bd.rowSpan)

      override def update(newBlueprint: WBlueprint) = {
        if (bd.colSpan != newBlueprint.colSpan || bd.rowSpan != newBlueprint.rowSpan)
          send(SetSpans(newBlueprint.colSpan, newBlueprint.rowSpan))
        super.update(newBlueprint)
      }
    }

    case class WBlueprint private[HeaderCell] (colSpan: Int, rowSpan: Int)(content: List[Blueprint])
        extends TableCellBlueprint {
      type P     = TableHeaderCellProtocol.type
      type Proxy = WProxy
      type This  = WBlueprint

      override def children = content

      override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)

      def colSpan(span: Int): This = WBlueprint(span, rowSpan)(content)

      def rowSpan(span: Int): This = WBlueprint(colSpan, span)(content)
    }

    override def widgetProtocol = TableHeaderCellProtocol

    def apply(content: Blueprint*) = WBlueprint(1, 1)(content.toList)
  }

  object TableCellBlueprint {
    import scala.language.implicitConversions

    implicit def a2cell[A](a: A)(implicit f: A => Blueprint): TableCellBlueprint = Cell(f(a))
  }
}
