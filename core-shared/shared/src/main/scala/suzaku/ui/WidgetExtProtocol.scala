package suzaku.ui

import arteria.core.{Message, Protocol}
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.StyleProperty

object WidgetExtProtocol extends Protocol {
  import boopickle.Default._

  implicit val stylePropertyPickler  = StyleProperty.stylePickler
  implicit val layoutPropertyPickler = LayoutProperty.layoutPickler

  sealed trait WidgetMessage extends Message

  case class UpdateStyle(params: List[(StyleProperty, Boolean)]) extends WidgetMessage

  case class UpdateLayout(params: List[LayoutProperty]) extends WidgetMessage

  val wmPickler = compositePickler[WidgetMessage]
    .addConcreteType[UpdateStyle]
    .addConcreteType[UpdateLayout]

  override type ChannelContext = Unit

  implicit val (messagePickler, witnessMsg) = defineProtocol(wmPickler)

  implicit val contextPickler = implicitly[Pickler[Unit]]
}

