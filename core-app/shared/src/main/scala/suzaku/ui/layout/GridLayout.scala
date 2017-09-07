package suzaku.ui.layout

import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._
import suzaku.ui.style.LengthDimension

import scala.language.implicitConversions

object GridLayout extends WidgetProtocolProvider {
  import GridLayoutProtocol._

  class WProxy private[GridLayout] (bd: WBlueprint)(widgetId: Int, uiChannel: UIChannel)
      extends WidgetProxy(GridLayoutProtocol, bd, widgetId, uiChannel) {

    override def process = {
      case message =>
        super.process(message)
    }

    override def initWidget = ChannelContext(bd.grid)

    override def update(newBlueprint: WBlueprint) = {
      if (newBlueprint.grid != blueprint.grid)
        send(SetGrid(newBlueprint.grid))
      super.update(newBlueprint)
    }
  }

  case class WBlueprint private[GridLayout] (grid: GridDef)(content: List[Blueprint]) extends WidgetBlueprint {
    type P     = GridLayoutProtocol.type
    type Proxy = WProxy
    type This  = WBlueprint

    override val children = content

    override def createProxy(widgetId: Int, uiChannel: UIChannel) = new WProxy(this)(widgetId, uiChannel)
  }

  override def widgetProtocol = GridLayoutProtocol

  def apply(grid: GridDef)(content: Blueprint*): WBlueprint = WBlueprint(grid)(content.toList)

  def apply(cols: TrackTemplate, rows: TrackTemplate, slots: Seq[Seq[LayoutId]])(content: Blueprint*): WBlueprint =
    WBlueprint(GridDef(cols, rows, slots))(content.toList)
}

trait GridImplicits {
  import suzaku.ui.layout.GridLayoutProtocol._

  implicit def length2track(size: LengthDimension): TrackTemplate = TrackTemplate(TrackSize(size))

  implicit def minmax2track(minmax: (LengthDimension, LengthDimension)): TrackTemplate =
    TrackTemplate(TrackMinMax(minmax._1, minmax._2))
}
