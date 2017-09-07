package suzaku.platform.web

import org.scalajs.dom
import org.scalajs.dom.raw.DedicatedWorkerGlobalScope
import suzaku.platform.Transport

import scala.scalajs.js.typedarray.ArrayBuffer

abstract class AppEntry {
  import DedicatedWorkerGlobalScope.self

  private var transport: WebWorkerTransport = _

  def main(args: Array[String]): Unit = {
    // create transport
    transport = new WorkerTransport(self)
    // receive WebWorker messages
    self.onmessage = onMessage _
    // create the actual application
    start(transport)
  }

  def start(transport: Transport): Any

  private def onMessage(msg: dom.MessageEvent): Unit = {
    msg.data match {
      case buffer: ArrayBuffer =>
        transport.receive(buffer)
      case _ => // ignore other messages
    }
  }
}
