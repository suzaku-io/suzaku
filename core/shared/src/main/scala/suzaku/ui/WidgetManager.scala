package suzaku.ui

import arteria.core._
import suzaku.platform.{Logger, Platform}
import suzaku.ui.UIProtocol._
import suzaku.ui.style.{InheritClasses, StyleBaseProperty}

import scala.collection.immutable.IntMap

abstract class WidgetManager(logger: Logger, platform: Platform)
    extends MessageChannelHandler[UIProtocol.type]
    with WidgetParent {
  import WidgetManager._

  private var builders             = Map.empty[String, WidgetBuilder[_ <: Protocol]]
  private var uiChannel: UIChannel = _
  protected var nodes              = IntMap[WidgetNode](-1 -> WidgetNode(emptyWidget(-1), Nil, -1))
  protected var rootNode           = Option.empty[WidgetNode]
  protected var styleInheritance   = IntMap.empty[List[Int]]
  protected var frameRequested     = false

  override def establishing(channel: MessageChannel[ChannelProtocol]) =
    uiChannel = channel

  def registerWidget(id: String, builder: WidgetBuilder[_ <: Protocol]): Unit =
    builders += id -> builder

  def registerWidget(clazz: Class[_], builder: WidgetBuilder[_ <: Protocol]): Unit =
    builders += clazz.getName -> builder

  def buildWidget(widgetType: String,
                  widgetId: Int,
                  channelId: Int,
                  globalId: Int,
                  channelReader: ChannelReader): Option[Widget] = {
    builders.get(widgetType).map(builder => builder.materialize(widgetId, channelId, globalId, uiChannel, channelReader))
  }

  def shouldRenderFrame: Boolean = {
    if (frameRequested) {
      frameRequested = false
      true
    } else {
      false
    }
  }

  def setParent(node: WidgetNode, parent: WidgetParent): Unit = {
    node.widget.setParent(parent)
    node.children.foreach(c => setParent(nodes(c), node.widget))
  }

  def reapplyStyles(id: Int): Unit = {
    nodes.get(id) match {
      case Some(node) =>
        node.widget.reapplyStyles()
        node.children.foreach(reapplyStyles)
      case None =>
      // no action
    }
  }

  override def process = {
    case MountRoot(widgetId) =>
      nodes.get(widgetId) match {
        case Some(node) =>
          rootNode = Some(node)
          setParent(node, this)
          mountRoot(node.widget.artifact)
        case None =>
          throw new IllegalArgumentException(s"Widget with id $widgetId has no node")
      }

    case RequestFrame =>
      frameRequested = true

    case SetChildren(widgetId, children) =>
      logger.debug(s"Setting [$children] as children of [$widgetId]")
      val childNodes = children.flatMap(nodes.get(_))
      nodes.get(widgetId).foreach { node =>
        node.widget.setChildren(childNodes.map(_.widget).asInstanceOf[Seq[node.widget.W]])
        childNodes.foreach(c => setParent(c, node.widget))
        nodes = nodes.updated(widgetId, node.copy(children = children))
      }

    case UpdateChildren(widgetId, ops) =>
      logger.debug(s"Updating children of [$widgetId] with [$ops]")
      nodes.get(widgetId).foreach { node =>
        // play operations on node children sequence first
        val cur    = node.children.toBuffer
        var curIdx = 0
        ops.foreach {
          case NoOp(n) =>
            curIdx += n
          case InsertOp(id) =>
            val widget = nodes(id).widget
            widget.setParent(node.widget)
            cur.insert(curIdx, id)
            curIdx += 1
          case RemoveOp(n) =>
            cur.remove(curIdx, n)
          case MoveOp(idx) =>
            cur.insert(curIdx, cur.remove(idx))
            curIdx += 1
          case ReplaceOp(id) =>
            cur(curIdx) = id
            curIdx += 1
        }
        // let widget update its child structure
        node.widget.updateChildren(ops, widgetId => nodes(widgetId).widget.asInstanceOf[node.widget.W])
        // update the widget node
        nodes = nodes.updated(widgetId, node.copy(children = cur))
      }

    case AddStyles(styles) =>
      logger.debug(s"Received styles $styles")
      var dirtyStyles = false
      val baseStyles = styles.reverse.map {
        case (styleId, props) =>
          // extract inheritance information
          val inherits = props.collect {
            case i: InheritClasses => i
          }
          val baseProps = props.collect {
            case prop: StyleBaseProperty => prop
          }
          if (inherits.nonEmpty) {
            dirtyStyles = true
            val resolved = inherits.flatMap(_.styles.flatMap(s => styleInheritance.getOrElse(s.id, List(s.id)))).distinct
            styleInheritance += styleId -> (resolved :+ styleId)
          }
          styleId -> baseProps
      }
      (dirtyStyles, rootNode) match {
        case (true, Some(node)) =>
          // set parent recursively to apply changed styles
          setParent(node, this)
        case _ => // nothing to update
      }
      addStyles(baseStyles)
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
      buildWidget(widgetType, widgetId, channelId, globalId, channelReader) match {
        case Some(widget) =>
          // add a node for the component
          nodes += widgetId -> WidgetNode(widget, Vector.empty, channelId)
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

  override def resolveStyleMapping(id: Int): List[Int] = {
    id :: Nil
  }

  override def resolveStyleInheritance(id: Int): List[Int] = {
    styleInheritance.getOrElse(id, id :: Nil)
  }

  protected def emptyWidget(widgetId: Int): Widget

  protected def mountRoot(node: WidgetArtifact): Unit

  protected def addStyles(styles: List[(Int, List[StyleBaseProperty])]): Unit
}

object WidgetManager {
  case class WidgetNode(widget: Widget, children: Seq[Int], channelId: Int)
}
