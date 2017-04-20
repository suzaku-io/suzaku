package webdemo

import suzaku.platform.web.{WebWorkerTransport, WorkerTransport}
import org.scalajs.dom
import org.scalajs.dom.raw.DedicatedWorkerGlobalScope

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.typedarray.ArrayBuffer

@JSExportTopLevel("WebDemoAppEntry")
object WebDemoAppEntry {
  import DedicatedWorkerGlobalScope.self

  var transport: WebWorkerTransport = _

  @JSExport
  def entry(): Unit = {
    // create transport
    transport = new WorkerTransport(self)
    // receive WebWorker messages
    self.onmessage = onMessage _
    // create the actual application
    val app = new WebDemoApp(transport)
  }

  def onMessage(msg: dom.MessageEvent) = {
    msg.data match {
      case buffer: ArrayBuffer =>
        transport.receive(buffer)
    }
  }
}
