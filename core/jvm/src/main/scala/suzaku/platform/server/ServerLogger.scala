package suzaku.platform.server

import suzaku.platform.Logger

class ServerLogger extends Logger {
  override def debug(message: String): Unit = Console.println(s"DEBUG - $message")
  override def info(message: String): Unit  = Console.println(s"INFO  - $message")
  override def warn(message: String): Unit  = Console.println(s"WARN  - $message")
  override def error(message: String): Unit = Console.println(s"ERROR - $message")
}
