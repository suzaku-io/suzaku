package webdemo

import suzaku.platform.web.{WebWorkerTransport, WorkerTransport}
import org.scalajs.dom
import org.scalajs.dom.raw.DedicatedWorkerGlobalScope

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}

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

        println(s"App Received:")
        debugPrint(debugArray)
         */
        transport.receive(buffer)
      case _ => // ignore other messages
    }
  }
}
