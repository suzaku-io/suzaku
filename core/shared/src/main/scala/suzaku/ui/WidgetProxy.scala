package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.{CreateWidget, UIChannel}
import suzaku.ui.WidgetProtocol.{UpdateLayout, WidgetMessage}

abstract class WidgetProxy[P <: Protocol, BP <: WidgetBlueprint](protected val protocol: P,
                                                                 protected var blueprint: BP,
                                                                 viewId: Int,
                                                                 uiChannel: UIChannel)
    extends MessageChannelHandler[P] {

  private implicit val witnessMsg = new MessageWitness[WidgetMessage, P] {}
  protected var isClosed          = false
  protected val channel =
    uiChannel.createChannel(protocol)(
      this,
      initView,
      CreateWidget(blueprint.getClass.getName, viewId)
    )

  // send initial layout, if any
  if (blueprint._layout.nonEmpty)
    send(UpdateLayout(blueprint._layout.map(p => (p._2, false)).toList))

  @inline def send[A <: Message](message: A)(implicit ev: MessageWitness[A, P]): Unit = {
    channel.send(message)
  }

  protected def initView: ChannelProtocol#ChannelContext

  def update(newBlueprint: BP): Unit = {
    if (newBlueprint._layout.nonEmpty || blueprint._layout.nonEmpty) {
      // collect what was removed or updated
      val updated =
        (blueprint._layout.keySet ++ newBlueprint._layout.keySet).foldLeft(List.empty[(LayoutParameter, Boolean)]) {
          (update, cls) =>
            (blueprint._layout.get(cls), newBlueprint._layout.get(cls)) match {
              case (Some(param), Some(newParam)) if newParam != param =>
                (newParam, false) :: update // changed
              case (None, Some(newParam)) =>
                (newParam, false) :: update // added
              case (Some(param), None) =>
                (param, true) :: update // removed
              case _ =>
                update
            }
        }
      if (updated.nonEmpty)
        send(UpdateLayout(updated))
    }
    blueprint = newBlueprint
  }

  def destroyView(): Unit = {
    if (isClosed)
      throw new Exception("Already closed")
    channel.close()
    isClosed = true
  }
}
