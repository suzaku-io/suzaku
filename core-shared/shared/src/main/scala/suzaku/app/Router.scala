package suzaku.app

import boopickle.Default._

trait RouterMessage

case object CreateUIChannel extends RouterMessage

case object CreateLoggerChannel extends RouterMessage

object RouterMessage {
  val defaultRouterPickler =
    compositePickler[RouterMessage].addConcreteType[CreateUIChannel.type].addConcreteType[CreateLoggerChannel.type]
}
