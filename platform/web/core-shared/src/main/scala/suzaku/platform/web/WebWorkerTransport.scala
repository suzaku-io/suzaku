package suzaku.platform.web

import java.nio.ByteBuffer

import suzaku.platform.Transport
import org.scalajs.dom
import org.scalajs.dom.webworkers.{DedicatedWorkerGlobalScope, Worker}

import scala.scalajs.js
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}

abstract class WebWorkerTransport extends Transport {
  private val emptyHandler                    = (data: ByteBuffer) => ()
  private var dataHandler: ByteBuffer => Unit = emptyHandler

  override def subscribe(handler: ByteBuffer => Unit): () => Unit = {
    dataHandler = handler
    // return an unsubscribing function
    () =>
      {
        // restore our empty handler when called
        dataHandler = emptyHandler
      }
  }

  def receive(data: ArrayBuffer): Unit = dataHandler(TypedArrayBuffer.wrap(data))
}

class WorkerTransport(worker: DedicatedWorkerGlobalScope) extends WebWorkerTransport {
  override def send(data: ByteBuffer): Unit = {
    assert(data.hasTypedArray())
    val out = data.typedArray.buffer.slice(0, data.limit())
    worker.postMessage(out, js.Array(out).asInstanceOf[js.UndefOr[js.Array[dom.raw.Transferable]]])
  }
}

class WorkerClientTransport(worker: Worker) extends WebWorkerTransport {
  override def send(data: ByteBuffer): Unit = {
    assert(data.hasTypedArray())
    val out = data.typedArray.buffer.slice(0, data.limit())
    worker.postMessage(out, js.Array(out).asInstanceOf[js.UndefOr[js.Array[dom.raw.Transferable]]])
  }
}
