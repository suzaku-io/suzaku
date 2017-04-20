package suzaku.app

import arteria.core._
import boopickle.Default._
import suzaku.ui.UIProtocol
import suzaku.util.LoggerProtocol

trait RouterMessage

case object CreateUIChannel extends RouterMessage

case object CreateLoggerChannel extends RouterMessage

object RouterMessage {
  val defaultRouterPickler =
    compositePickler[RouterMessage].addConcreteType[CreateUIChannel.type].addConcreteType[CreateLoggerChannel.type]
}

class AppRouterHandler(uiHandler: MessageChannelHandler[UIProtocol.type]) extends MessageRouterHandler[RouterMessage] {
  override def materializeChildChannel(id: Int,
                                       globalId: Int,
                                       router: MessageRouterBase,
                                       materializeChild: RouterMessage,
                                       contextReader: ChannelReader): Option[MessageChannelBase] = {
    materializeChild match {
      case CreateUIChannel =>
        val context = contextReader.read[UIProtocol.ChannelContext]
        Some(new MessageChannel(UIProtocol)(id, globalId, router, uiHandler, context))
      case _ =>
        None
    }
  }
}

class UIRouterHandler(loggerHandler: MessageChannelHandler[LoggerProtocol.type])
    extends MessageRouterHandler[RouterMessage] {
  override def materializeChildChannel(id: Int,
                                       globalId: Int,
                                       router: MessageRouterBase,
                                       materializeChild: RouterMessage,
                                       contextReader: ChannelReader): Option[MessageChannelBase] = {
    materializeChild match {
      case CreateLoggerChannel =>
        val context = contextReader.read[LoggerProtocol.ChannelContext]
        Some(new MessageChannel(LoggerProtocol)(id, globalId, router, loggerHandler, context))
      case _ =>
        None
    }
  }
}
