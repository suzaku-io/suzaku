package suzaku.platform.web

import org.scalajs.dom
import org.scalajs.dom.raw.Worker
import suzaku.app.UIBase
import suzaku.platform.Transport

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.typedarray.ArrayBuffer

abstract class UIEntry {
  private var transport: WebWorkerTransport = _

  @JSExport
  def entry(workerScript: String): Unit = {
    // create the worker to run our application in
    val worker = new Worker(workerScript)

    // create the transport
    transport = new WorkerClientTransport(worker)

    // listen to messages from worker
    worker.onmessage = onMessage _

    start(transport)
  }

  def start(transport: Transport): UIBase

  private def onMessage(msg: dom.MessageEvent): Unit = {
    msg.data match {
      case buffer: ArrayBuffer =>
        transport.receive(buffer)
      case _ => // ignore other messages
    }
  }
}
