package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui._
import suzaku.ui.resource.ImageResource

object ButtonProtocol extends WidgetProtocol {

  sealed trait ButtonMessage extends Message

  case class SetLabel(label: String) extends ButtonMessage

  case class SetIcon(icon: Option[ImageResource]) extends ButtonMessage

  case object Click extends ButtonMessage

  import ImageResource.pickler

  val mPickler = compositePickler[ButtonMessage]
    .addConcreteType[SetLabel]
    .addConcreteType[SetIcon]
    .addConcreteType[Click.type]

  implicit val (messagePickler, witnessMsg1, witnessMsg2) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext(label: String, icon: Option[ImageResource])

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}
