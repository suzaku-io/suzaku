package suzaku.util

import arteria.core.MessageChannelHandler
import suzaku.platform.Logger
import suzaku.util.LoggerProtocol._

class LoggerHandler(logger: Logger) extends MessageChannelHandler[LoggerProtocol.type] {
  override def process = {
    case LogDebug(msg) => logger.debug(msg)
    case LogInfo(msg)  => logger.info(msg)
    case LogWarn(msg)  => logger.warn(msg)
    case LogError(msg) => logger.error(msg)
  }
}
