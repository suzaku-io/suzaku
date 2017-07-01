package suzaku.app

import java.nio.ByteBuffer

import arteria.core._
import boopickle.DefaultBasic._
import suzaku.platform.{Logger, Platform, Scheduler, Transport}
import suzaku.ui.UIProtocol.NextFrame
import suzaku.ui._
import suzaku.util.LoggerHandler

abstract class UIBase(transport: Transport,
                      handler: (LoggerHandler) => UIRouterHandler = loggerHandler => new UIRouterHandler(loggerHandler))(
    implicit routerPickler: Pickler[RouterMessage] = RouterMessage.defaultRouterPickler) {

  // take an instance of the current Platform
  def platform: Platform

  // initialize with a platform specific logger and scheduler
  val logger        = platform.logger
  val loggerHandler = new LoggerHandler(logger)
  val scheduler     = platform.scheduler
  // initialize the router
  val router = new MessageRouter[RouterMessage](handler(loggerHandler), true)

  // constructor
  // subscribe to messages from transport
  transport.subscribe(receive)
  // create the UI channel
  val widgetRenderer = platform.widgetRenderer(logger)
  val uiManagerChannel =
    router.createChannel(UIProtocol)(widgetRenderer, UIProtocol.UIProtocolContext(), CreateUIChannel)
  // send out first messages to establish router channel
  transport.send(router.flush())
  // create a frame timer to flush messages on every frame update
  val cancelFrameScheduler = scheduler.scheduleFrame(time => nextFrame(time))

  private def nextFrame(time: Long): Unit = {
    if (widgetRenderer.isFrameComplete && (router.hasPending || widgetRenderer.shouldRenderFrame)) {
      widgetRenderer.nextFrame(time)
      uiManagerChannel.send(NextFrame(time))
      transport.send(router.flush())
    }
  }

  def receive(data: ByteBuffer): Unit = {
    val a = new Array[Byte](data.remaining())
    data.slice().get(a)
    // logger.debug(s"Received " + a.map(_.toChar).mkString(" "))
    // logger.debug(s"Received " + a.map("%02x" format _).mkString(" "))
    router.receive(data)
    // send pending messages
    if (router.hasPending) {
      transport.send(router.flush())
    }
  }

  protected def main(): Unit

  // start application
  main()
}
