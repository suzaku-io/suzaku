package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.{CreateWidget, UIChannel}
import suzaku.ui.WidgetExtProtocol.{ListenTo, OnClick, OnFocusChange, OnLongClick, UpdateLayout, UpdateStyle, WidgetMessage}
import suzaku.ui.style.StyleProperty

abstract class WidgetProxy[P <: WidgetProtocol, BP <: WidgetBlueprint](protected val protocol: P,
                                                                       protected var blueprint: BP,
                                                                       widgetId: Int,
                                                                       uiChannel: UIChannel)
    extends MessageChannelHandler[P] {

  private implicit val witnessMsg = new MessageWitness[WidgetMessage, P] {}
  protected var isClosed          = false
  protected val channel =
    uiChannel.createChannel(protocol)(
      this,
      initWidget,
      CreateWidget(
        UIManagerProxy.getWidgetClass(protocol),
        widgetId
      )
    )

  // send initial style, layout and event listener registration, if any
  if (blueprint._style.nonEmpty)
    send(UpdateStyle(blueprint._style.map(p => (p._2, false)).toList))
  if (blueprint._layout.nonEmpty)
    send(UpdateLayout(blueprint._layout))
  if (blueprint._activeEvents.nonEmpty)
    blueprint._activeEvents.foreach(event => send(ListenTo(event)))

  @inline def send[A <: Message](message: A)(implicit ev: MessageWitness[A, P]): Unit = {
    channel.send(message)
  }

  /**
    * Process common widget messages
    */
  override def process = {
    case OnClick(x, y, button) =>
      blueprint._onClickHandler.foreach(_(x, y, button))
    case OnLongClick(x, y, button) =>
      blueprint._onLongClickHandler.foreach(_(x, y, button))
    case OnFocusChange(focused) =>
      blueprint._onFocusChangeHandler.foreach(_(focused))
  }

  protected def initWidget: ChannelProtocol#ChannelContext

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
    // update event listener registrations
    if (newBlueprint._activeEvents != blueprint._activeEvents) {
      newBlueprint._activeEvents.diff(blueprint._activeEvents).foreach(event => send(ListenTo(event)))
      blueprint._activeEvents.diff(newBlueprint._activeEvents).foreach(event => send(ListenTo(event, active = false)))
    }

    blueprint = newBlueprint
  }

  def destroyWidget(): Unit = {
    if (isClosed)
      throw new Exception("Already closed")
    channel.close()
    isClosed = true
  }
}
