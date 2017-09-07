package suzaku.app

import java.nio.ByteBuffer

import arteria.core._
import boopickle.DefaultBasic._
import suzaku.platform.{Platform, Transport}
import suzaku.ui.UIProtocol.NextFrame
import suzaku.ui._
import suzaku.util.{LoggerHandler, LoggerProtocol}

import scala.collection.mutable

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

  val receiveBuffers = mutable.ListBuffer.empty[ByteBuffer]

  // constructor
  // subscribe to messages from transport
  transport.subscribe(receive)
  // create the UI channel
  val widgetManager = platform.widgetManager(logger)
  val uiManagerChannel =
    router.createChannel(UIProtocol)(widgetManager, UIProtocol.UIProtocolContext(), CreateUIChannel)
  // send out first messages to establish router channel
  transport.send(router.flush())
  // create a frame timer to flush messages on every frame update
  val cancelFrameScheduler = scheduler.scheduleFrame(time => nextFrame(time))

  private def nextFrame(time: Long): Unit = {
    // only send NextFrame if we got something from app, or there is a request for it
    if (receiveBuffers.nonEmpty || widgetManager.shouldRenderFrame) uiManagerChannel.send(NextFrame(time))
    // send messages before processing incoming messages so that the app thread can run in parallel to the UI thread
    if (router.hasPending) transport.send(router.flush())
    widgetManager.nextFrame(time)
    // process pending receive buffers
    receiveBuffers.foreach(router.receive)
    receiveBuffers.clear()
  }

  def receive(data: ByteBuffer): Unit = {
    /*
    val a = new Array[Byte](data.remaining())
    data.slice().get(a)
    logger.debug(s"Received " + a.map(_.toChar).mkString(" "))
    logger.debug(s"Received " + a.map("%02x" format _).mkString(" "))
     */
    receiveBuffers += data
  }

  protected def main(): Unit

  // start application
  main()
}

class UIRouterHandler(loggerHandler: MessageChannelHandler[LoggerProtocol.type])
  extends MessageRouterHandler[RouterMessage] {

  override def materializeChildChannel(id: Int,
    globalId: Int,
    router: MessageRouterBase,
    materializeChild: RouterMessage,
    contextReader: ChannelReader): Option[MessageChannelBase] = {
    materializeChild match {
      case CreateLoggerChannel =>
        val context = contextReader.read[LoggerProtocol.ChannelContext](LoggerProtocol.contextPickler)
        Some(new MessageChannel(LoggerProtocol)(id, globalId, router, loggerHandler, context))
      case _ =>
        None
    }
  }
}
