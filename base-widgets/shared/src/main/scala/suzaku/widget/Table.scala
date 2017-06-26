package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._

object TableProtocol extends Protocol {

  sealed trait TableMessage extends Message

  val mPickler = compositePickler[TableMessage]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext()

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object Table extends WidgetBlueprintProvider {
  class WProxy private[Table] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableProtocol, bd, widgetId, uiChannel) {
    import TableProtocol._

    override def process = {
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[Table] ()(body: TableBody.WBlueprint,
                                          header: Option[TableHeader.WBlueprint],
                                          footer: Option[TableFooter.WBlueprint])
      extends WidgetBlueprint {
    type P     = TableProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = header.getOrElse(EmptyBlueprint) :: body :: footer.getOrElse(EmptyBlueprint) :: Nil

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)

    def apply(footer: TableFooter.WBlueprint): WBlueprint = {
      assert(body.children.size == footer.children.size)
      this.copy()(body, header, Some(footer))
    }

    def footer(footer: TableFooter.WBlueprint): WBlueprint = {
      assert(body.children.size == footer.children.size)
      this.copy()(body, header, Some(footer))
    }
  }

  override def blueprintClass = classOf[WBlueprint]

  class TableBuilder {
    def apply(header: TableHeader.WBlueprint): TableBuilderHeader  = new TableBuilderHeader(header)
    def header(header: TableHeader.WBlueprint): TableBuilderHeader = new TableBuilderHeader(header)

    def apply(body: TableBody.WBlueprint): WBlueprint = WBlueprint()(body, None, None)
    def body(body: TableBody.WBlueprint): WBlueprint  = WBlueprint()(body, None, None)
  }

  class TableBuilderHeader(header: TableHeader.WBlueprint) {
    def apply(body: TableBody.WBlueprint): WBlueprint = {
      WBlueprint()(body, Some(header), None)
    }

    def body(body: TableBody.WBlueprint): WBlueprint = {
      WBlueprint()(body, Some(header), None)
    }
  }

  def apply(): TableBuilder                                     = new TableBuilder
  def apply(header: TableHeader.WBlueprint): TableBuilderHeader = new TableBuilderHeader(header)
  def apply(body: TableBody.WBlueprint): WBlueprint             = WBlueprint()(body, None, None)
}

object TableBaseProtocol extends Protocol {

  sealed trait TableMessage extends Message

  val mPickler = compositePickler[TableMessage]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  case class ChannelContext()

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object TableHeader extends WidgetBlueprintProvider {
  class WProxy private[TableHeader] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableBaseProtocol, bd, widgetId, uiChannel) {
    import TableBaseProtocol._

    override def process = {
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TableHeader] ()(columns: List[Blueprint]) extends WidgetBlueprint {
    type P     = TableBaseProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = columns

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(colName: String, colNames: String*) = WBlueprint()(Text(colName) :: colNames.map(Text(_)).toList)

  def apply(col: Blueprint, cols: Blueprint*) = WBlueprint()(col :: cols.toList)
}

object TableBody extends WidgetBlueprintProvider {
  class WProxy private[TableBody] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableBaseProtocol, bd, widgetId, uiChannel) {
    import TableBaseProtocol._

    override def process = {
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TableBody] ()(rows: List[TableRow.WBlueprint]) extends WidgetBlueprint {
    type P     = TableBaseProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def children = rows

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)

  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(row: TableRow.WBlueprint, rows: TableRow.WBlueprint*): WBlueprint = WBlueprint()(row :: rows.toList)

}

object TableFooter extends WidgetBlueprintProvider {
  class WProxy private[TableFooter] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableBaseProtocol, bd, widgetId, uiChannel) {
    import TableBaseProtocol._

    override def process = {
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TableFooter] ()(columns: List[Blueprint]) extends WidgetBlueprint {
    type P     = TableBaseProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def children = columns

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(colName: String, colNames: String*) = WBlueprint()(Text(colName) :: colNames.map(Text(_)).toList)

  def apply(col: Blueprint, cols: Blueprint*) = WBlueprint()(col :: cols.toList)
}

object TableRow extends WidgetBlueprintProvider {
  class WProxy private[TableRow] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableBaseProtocol, bd, widgetId, uiChannel) {
    import TableBaseProtocol._

    override def process = {
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TableRow] ()(cells: List[TableCell.WBlueprint]) extends WidgetBlueprint {
    type P     = TableBaseProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def children = cells

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(cell: TableCell.WBlueprint, cells: TableCell.WBlueprint*) = WBlueprint()(cell :: cells.toList)

}

object TableCell extends WidgetBlueprintProvider {
  class WProxy private[TableCell] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(TableBaseProtocol, bd, widgetId, uiChannel) {
    import TableBaseProtocol._

    override def process = {
      case message =>
        super.process(message)
    }

    override protected def initWidget = ChannelContext()

    override def update(newBlueprint: WBlueprint) = {
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[TableCell] ()(content: List[Blueprint]) extends WidgetBlueprint {
    type P     = TableBaseProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override def children = content

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new Proxy(this)(widgetId, uiChannel)
  }

  override def blueprintClass = classOf[WBlueprint]

  def apply(content: Blueprint*) = WBlueprint()(content.toList)
}
