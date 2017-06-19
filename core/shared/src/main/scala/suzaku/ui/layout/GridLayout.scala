package suzaku.ui.layout

import arteria.core.{Message, Protocol}
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui._
import suzaku.ui.style.LengthDimension

import scala.language.implicitConversions

object GridLayoutProtocol extends Protocol {
  import boopickle.Default._
  import LengthDimension._

  implicit val layoutIdPickler = LayoutId.LayoutIdPickler

  sealed trait TrackDef

  case class TrackSize(size: LengthDimension)                        extends TrackDef
  case class TrackMinMax(min: LengthDimension, max: LengthDimension) extends TrackDef

  case class TrackTemplate(tracks: List[TrackDef]) {
    def ~(size: LengthDimension): TrackTemplate =
      copy(tracks = tracks :+ TrackSize(size))

    def ~(minmax: (LengthDimension, LengthDimension)): TrackTemplate =
      copy(tracks = tracks :+ TrackMinMax(minmax._1, minmax._2))
  }

  object TrackTemplate {
    def apply(tracks: TrackDef*): TrackTemplate = TrackTemplate(tracks.toList)
  }

  case class GridDef(cols: TrackTemplate, rows: TrackTemplate, slots: Seq[Seq[LayoutId]])

  sealed trait LayoutMessage extends Message

  case class SetGrid(grid: GridDef) extends LayoutMessage

  private val mPickler = compositePickler[LayoutMessage]
    .addConcreteType[SetGrid]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetProtocol.wmPickler)

  final case class ChannelContext(grid: GridDef)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}

object GridLayout extends WidgetBlueprintProvider {
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

  override def blueprintClass = classOf[WBlueprint]

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
