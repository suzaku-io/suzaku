package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.{CreateWidget, UIChannel}
import suzaku.ui.WidgetProtocol.{UpdateLayout, UpdateStyle, WidgetMessage}
import suzaku.ui.style.StyleProperty

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
      CreateWidget(
        UIManager.getWidgetClass(blueprint.getClass, uiChannel),
        viewId
      )
    )

  // send initial style and layout, if any
  if (blueprint._style.nonEmpty)
    send(UpdateStyle(blueprint._style.map(p => (p._2, false)).toList))
  if (blueprint._layout.nonEmpty)
    send(UpdateLayout(blueprint._layout))

  @inline def send[A <: Message](message: A)(implicit ev: MessageWitness[A, P]): Unit = {
    channel.send(message)
  }

  protected def initView: ChannelProtocol#ChannelContext

  def update(newBlueprint: BP): Unit = {
    // update style
    if (newBlueprint._style.nonEmpty || blueprint._style.nonEmpty) {
      // println(s"Updating style: ${blueprint._style} -> ${newBlueprint._style} ")
      // collect what was removed or updated
      val updated =
        (blueprint._style.keySet ++ newBlueprint._style.keySet).foldLeft(List.empty[(StyleProperty, Boolean)]) {
          (update, cls) =>
            (blueprint._style.get(cls), newBlueprint._style.get(cls)) match {
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
        send(UpdateStyle(updated))
    }
    // update layout properties
    if (newBlueprint._layout != blueprint._layout) {
      send(UpdateLayout(newBlueprint._layout))
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
