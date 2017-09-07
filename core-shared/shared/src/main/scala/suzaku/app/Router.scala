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
