package suzaku.ui

import arteria.core._
import suzaku.platform.Logger
import suzaku.ui.UIProtocol._
import suzaku.ui.style.StyleRegistry.StyleRegistration

import scala.collection.immutable.IntMap

abstract class WidgetRenderer(logger: Logger) extends MessageChannelHandler[UIProtocol.type] {
  import WidgetRenderer._

  private var builders             = Map.empty[String, WidgetBuilder[_ <: Protocol]]
  private var uiChannel: UIChannel = _

  protected def emptyWidget: Widget
  protected var nodes          = IntMap[WidgetNode](-1 -> WidgetNode(-1, emptyWidget, Nil, -1))
  protected var frameRequested = false

  override def establishing(channel: MessageChannel[ChannelProtocol]) =
    uiChannel = channel

  def registerWidget(id: String, builder: WidgetBuilder[_ <: Protocol]): Unit =
    builders += id -> builder

  def registerWidget(clazz: Class[_], builder: WidgetBuilder[_ <: Protocol]): Unit =
    builders += clazz.getName -> builder

  def buildWidget(widgetType: String, channelId: Int, globalId: Int, channelReader: ChannelReader): Option[Widget] = {
    builders.get(widgetType).map(builder => builder.materialize(channelId, globalId, uiChannel, channelReader))
  }

  def shouldRenderFrame: Boolean = {
    if (frameRequested) {
      frameRequested = false
      true
    } else {
      false
    }
  }

  override def process = {
    case MountRoot(widgetId) =>
      nodes.get(widgetId) match {
        case Some(node) =>
          mountRoot(node.widget.artifact)
        case None =>
          throw new IllegalArgumentException(s"Widget with id $widgetId has no node")
      }

    case RequestFrame =>
      frameRequested = true

    case SetChildren(widgetId, children) =>
      logger.debug(s"Setting [$children] as children of [$widgetId]")
      val childArtifacts = children.flatMap(nodes.get(_)).map(_.widget.artifact)
      nodes.get(widgetId).foreach { node =>
        node.widget.setChildren(childArtifacts.asInstanceOf[Seq[node.widget.Artifact]])
        nodes = nodes.updated(widgetId, node.copy(children = children))
      }

    case UpdateChildren(widgetId, ops) =>
      logger.debug(s"Updating children of [$widgetId] with [$ops]")
      nodes.get(widgetId).foreach { node =>
        node.widget.updateChildren(ops, widgetId => nodes(widgetId).widget.asInstanceOf[node.widget.V])
      }

    case AddStyles(styles) =>
      addStyles(styles)

  }

  override def materializeChildChannel(channelId: Int,
                                       globalId: Int,
                                       parent: MessageChannelBase,
                                       channelReader: ChannelReader) = {
    import boopickle.Default._
    // read the component creation data
    val CreateWidget(widgetType, widgetId) = channelReader.read[CreateWidget]

    logger.debug(f"Building widget $widgetType on channel [$channelId, $globalId%08x]")
    try {
      buildWidget(widgetType, channelId, globalId, channelReader) match {
        case Some(widget) =>
          // add a node for the component
          nodes += widgetId -> WidgetNode(widgetId, widget, Vector.empty, channelId)
          widget.channel
        case None =>
          throw new IllegalAccessException(s"Unable to materialize a widget '$widgetType'")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unhandled exception while building widget $widgetType: $e")
        throw e
    }
  }

  override def channelWillClose(id: Int): Unit = {
    logger.debug(s"Widget [$id] removed")
    nodes -= id
  }

  def mountRoot(node: WidgetArtifact): Unit

  def addStyles(styles: List[StyleRegistration]): Unit
}

object WidgetRenderer {
  case class WidgetNode(id: Int, widget: Widget, children: Seq[Int], channelId: Int)
}

abstract class WidgetArtifact

abstract class Widget {
  type CP <: Protocol
  type Artifact <: WidgetArtifact
  type V <: Widget

  private var messageChannel: MessageChannel[CP] = _

  protected[suzaku] def withChannel(channel: MessageChannel[CP]): this.type = {
    messageChannel = channel
    this
  }

  def channel: MessageChannel[CP] = messageChannel

  def artifact: Artifact

  def setChildren(children: Seq[Artifact]): Unit =
    throw new NotImplementedError("This widget cannot have children")

  def updateChildren(ops: Seq[ChildOp], widget: Int => V): Unit

  def mapStyle(id: Int): Int = id
}

abstract class WidgetWithProtocol[P <: Protocol] extends Widget with MessageChannelHandler[P] {
  override type CP = P
}

abstract class WidgetBuilder[P <: Protocol](protocol: P) {
  protected def create(context: P#ChannelContext): WidgetWithProtocol[P]

  def materialize(id: Int, globalId: Int, parent: MessageChannelBase, channelReader: ChannelReader): Widget = {
    val context = channelReader.read(protocol.contextPickler)
    val widget  = create(context)
    val channel = new MessageChannel(protocol)(id, globalId, parent, widget, context)
    widget.withChannel(channel)
  }
}
