package suzaku.ui.layout

import arteria.core._
import suzaku.ui._

object LinearLayoutProtocol extends WidgetProtocol {
  import boopickle.Default._

  sealed trait LayoutMessage extends Message

  final case class SetDirection(direction: Direction) extends LayoutMessage

  final case class SetJustify(justify: Justify) extends LayoutMessage

  final case class SetAlignment(align: Alignment) extends LayoutMessage

  import Alignment._

  private val mPickler = compositePickler[LayoutMessage]
    .addConcreteType[SetDirection]
    .addConcreteType[SetJustify]
    .addConcreteType[SetAlignment]

  implicit val (messagePickler, witnessMsg1, widgetExtWitness) = defineProtocol(mPickler, WidgetExtProtocol.wmPickler)

  final case class ChannelContext(direction: Direction, justify: Justify, align: Alignment)

  override val contextPickler = implicitly[Pickler[ChannelContext]]
}
