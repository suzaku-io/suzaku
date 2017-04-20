package suzaku.util

import arteria.core._
import boopickle.Default._

object LoggerProtocol extends Protocol {

  case class LoggerProtocolContext()

  override type ChannelContext = LoggerProtocolContext

  sealed trait LoggerMessage extends Message

  case class LogDebug(message: String) extends LoggerMessage

  case class LogInfo(message: String) extends LoggerMessage

  case class LogWarn(message: String) extends LoggerMessage

  case class LogError(message: String) extends LoggerMessage

  private val logPickler = compositePickler[LoggerMessage]
    .addConcreteType[LogDebug]
    .addConcreteType[LogInfo]
    .addConcreteType[LogWarn]
    .addConcreteType[LogError]

  implicit val (messagePickler, logMsgWitness) = defineProtocol(logPickler)

  override val contextPickler = implicitly[Pickler[LoggerProtocolContext]]
}
