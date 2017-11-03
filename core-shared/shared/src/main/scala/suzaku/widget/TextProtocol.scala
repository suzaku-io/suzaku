package suzaku.widget

import arteria.core._
import boopickle.Default._
import suzaku.ui.{WidgetExtProtocol, WidgetProtocol}

object TextProtocol extends WidgetProtocol {
  sealed trait TextMessage extends Message

  case class SetText(text: String) extends TextMessage

  val tmPickler = compositePickler[TextMessage]
    .addConcreteType[SetText]

  implicit val (messagePickler, witnessMsg1, widgetExtWitness) = defineProtocol(tmPickler, WidgetExtProtocol.wmPickler)

  case class ChannelContext(text: String)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}
