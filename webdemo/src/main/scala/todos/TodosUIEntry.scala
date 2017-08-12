package todos

import org.scalajs.dom
import org.scalajs.dom.raw.Worker
import suzaku.platform.web.{WebWorkerTransport, WorkerClientTransport}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.typedarray.ArrayBuffer

@JSExportTopLevel("TodosUIEntry")
object TodosUIEntry {
  var transport: WebWorkerTransport = _

  @JSExport
  def entry(workerScript: String): Unit = {
    // create the worker to run our application in
    val worker = new Worker(workerScript)

    // create the transport
    transport = new WorkerClientTransport(worker)

    // listen to messages from worker
    worker.onmessage = onMessage _

    new TodosUI(transport)
  }

  private def debugPrint(data: Array[Byte]): Unit = {
    data.grouped(16).foreach { d =>
      val hex = d.map(c => "%02X " format (c & 0xFF)).mkString
      val str = d
        .collect {
          case ascii if ascii >= 0x20 && ascii < 0x80 => ascii
          case _                                      => '.'.toByte
        }
        .map(_.toChar)
        .mkString
      println(hex.padTo(16 * 3, ' ') + str)
    }
  }

  def onMessage(msg: dom.MessageEvent) = {
    msg.data match {
      case buffer: ArrayBuffer =>
        /*
        val debugData = TypedArrayBuffer.wrap(buffer.slice(0))
        val debugArray = Array.ofDim[Byte](debugData.limit())
        debugData.get(debugArray)

        println(s"UI Received:")
        debugPrint(debugArray)
         */
        transport.receive(buffer)
      case _ => // ignore other messages
    }
  }
}
