package suzaku.ui

import arteria.core._
import suzaku.platform.{Logger, Platform}
import suzaku.ui.UIProtocol._
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.StyleClassRegistry.StyleClassRegistration
import suzaku.ui.style.{ExtendClasses, InheritClasses, StyleBaseProperty}

import scala.collection.immutable.IntMap

abstract class WidgetManager(logger: Logger, platform: Platform)
    extends MessageChannelHandler[UIProtocol.type]
    with WidgetParent {
  import WidgetManager._

  private var widgetClassMap       = Map.empty[Int, String]
  private var registeredWidgets    = Map.empty[String, WidgetBuilder[_ <: Protocol]]
  private var builders             = Map.empty[Int, WidgetBuilder[_ <: Protocol]]
  private var uiChannel: UIChannel = _
  protected var nodes              = IntMap[WidgetNode](-1 -> WidgetNode(emptyWidget(-1), Nil, -1))
  protected var rootNode           = Option.empty[WidgetNode]
  protected var styleInheritance   = IntMap.empty[List[Int]]
  protected var registeredStyles   = IntMap.empty[List[StyleBaseProperty]]
  protected var themes             = Vector.empty[(Int, Map[Int, List[Int]])]
  protected var activeTheme        = Map.empty[Int, List[Int]]
  protected var frameRequested     = false

  override def establishing(channel: MessageChannel[ChannelProtocol]) =
    uiChannel = channel

  def registerWidget(id: String, builder: WidgetBuilder[_ <: Protocol]): Unit =
    registeredWidgets += id -> builder

  def registerWidget(clazz: Class[_], builder: WidgetBuilder[_ <: Protocol]): Unit =
    registeredWidgets += clazz.getName -> builder

  def buildWidget(widgetClass: Int,
                  widgetId: Int,
                  channelId: Int,
                  globalId: Int,
                  channelReader: ChannelReader): Option[Widget] = {
    val builder = builders.get(widgetClass) orElse {
      // update builder map from registered widgets
      val b = widgetClassMap.get(widgetClass).flatMap(registeredWidgets.get)
      b.foreach(v => builders += widgetClass -> v)
      b
    }
    builder.map(builder => builder.materialize(widgetId, widgetClass, channelId, globalId, uiChannel, channelReader))
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

  def rebuildThemes(themes: Seq[(Int, Map[Int, List[Int]])]): Unit = {
    // join themes to form the active theme
    activeTheme = themes.foldLeft(Map.empty[Int, List[Int]]) {
      case (act, (_, styleMap)) =>
        styleMap.foldLeft(act) {
          case (current, (widget, styleClasses)) =>
            current.updated(widget, (current.getOrElse(widget, Nil) ++ styleClasses).distinct)
        }
    }
    // reapply styles as theme changes may affect them
    rootNode.foreach(n => reapplyStyles(n.widget.widgetId))
  }

  def applyTheme(widgetClassId: Int): List[Int] = {
    activeTheme.getOrElse(widgetClassId, Nil)
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
            val widget = nodes(id).widget
            widget.setParent(node.widget)
            cur(curIdx) = id
            curIdx += 1
        }
        // let widget update its child structure
        node.widget.updateChildren(ops, widgetId => nodes(widgetId).widget.asInstanceOf[node.widget.W])
        // update the widget node
        nodes = nodes.updated(widgetId, node.copy(children = cur))
      }

    case AddStyles(styles) =>
      logger.debug(s"Received styles ${styles.reverse}")
      var dirtyStyles = false
      val baseStyles = styles.reverse.map {
        case StyleClassRegistration(styleId, styleName, props) =>
          // extract inheritance information
          val inherits = props.collect {
            case i: InheritClasses => i
          }
          val extend = props.collect {
            case e: ExtendClasses => e
          }
          val baseProps = props.collect {
            case prop: StyleBaseProperty => prop
          }
          val extProps = extend.flatMap(_.styles.flatMap(sc => registeredStyles.getOrElse(sc.id, Nil)))
          if (inherits.nonEmpty) {
            dirtyStyles = true
            val resolved = inherits.flatMap(_.styles.flatMap(s => styleInheritance.getOrElse(s.id, List(s.id)))).distinct
            styleInheritance += styleId -> (resolved :+ styleId)
          }
          val allProps = extProps ::: baseProps
          registeredStyles += styleId -> allProps

          (styleId, styleName, allProps)
      }
      (dirtyStyles, rootNode) match {
        case (true, Some(node)) =>
          // set parent recursively to apply changed styles
          setParent(node, this)
        case _ => // nothing to update
      }
      addStyles(baseStyles)

    case AddLayoutIds(ids) =>
      // TODO store somewhere

    case ActivateTheme(themeId, theme) =>
      themes :+= (themeId, theme)
      rebuildThemes(themes)

    case DeactivateTheme(themeId) =>
      themes = themes.filterNot(_._1 == themeId)
      rebuildThemes(themes)

    case RegisterWidgetClass(className, classId) =>
      widgetClassMap += classId -> className
  }

  override def materializeChildChannel(channelId: Int,
                                       globalId: Int,
                                       parent: MessageChannelBase,
                                       channelReader: ChannelReader): MessageChannelBase = {
    import boopickle.Default._
    // read the component creation data
    val CreateWidget(widgetClass, widgetId) = channelReader.read[CreateWidget]

    logger.debug(f"Building widget $widgetClass on channel [$channelId, $globalId%08x]")
    try {
      buildWidget(widgetClass, widgetId, channelId, globalId, channelReader) match {
        case Some(widget) =>
          // add a node for the component
          nodes += widgetId -> WidgetNode(widget, Vector.empty, channelId)
          widget.channel
        case None =>
          throw new IllegalAccessException(s"Unable to materialize a widget '$widgetClass'")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unhandled exception while building widget $widgetClass: $e")
        throw e
    }
  }

  override def channelWillClose(id: Int): Unit = {
    logger.debug(s"Widget [$id] removed")
    nodes -= id
  }

  override def resolveStyleMapping(ids: List[Int]): List[Int] = {
    ids
  }

  override def resolveStyleInheritance(ids: List[Int]): List[Int] = {
    val res = ids.flatMap(id => styleInheritance.getOrElse(id, id :: Nil))
    res
  }

  override def resolveLayout(widget: Widget, layoutProperties: List[LayoutProperty]): Unit = {}

  protected def emptyWidget(widgetId: Int): Widget

  protected def mountRoot(node: WidgetArtifact): Unit

  protected def addStyles(styles: List[(Int, String, List[StyleBaseProperty])]): Unit
}

object WidgetManager {
  case class WidgetNode(widget: Widget, children: Seq[Int], channelId: Int)
}
