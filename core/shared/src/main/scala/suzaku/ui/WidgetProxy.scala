package suzaku.ui

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol.{CreateWidget, UIChannel}

abstract class WidgetProxy[P <: Protocol, BP <: WidgetBlueprint](protected val protocol: P,
                                                                 protected var blueprint: BP,
                                                                 viewId: Int,
                                                                 uiChannel: UIChannel)
    extends MessageChannelHandler[P] {
  protected val channel =
    uiChannel.createChannel(protocol)(this, initView, CreateWidget(blueprint.getClass.getName, viewId))
  var isClosed = false

  def send[A <: Message](message: A)(implicit ev: MessageWitness[A, P]): Unit = {
    channel.send(message)
  }

  protected def initView: ChannelProtocol#ChannelContext

  def update(newBlueprint: BP): Unit = {
    blueprint = newBlueprint
  }

  def destroyView(): Unit = {
    if (isClosed)
      throw new Exception("Already clsoed")
    channel.close()
    isClosed = true
  }
}
