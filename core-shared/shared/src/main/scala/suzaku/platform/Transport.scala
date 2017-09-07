package suzaku.platform

import java.nio.ByteBuffer

trait Transport {
  def subscribe(handler: ByteBuffer => Unit): () => Unit

  def send(data: ByteBuffer): Unit
}
