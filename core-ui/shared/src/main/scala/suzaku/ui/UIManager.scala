package suzaku.ui

import arteria.core._
import suzaku.platform.{Logger, Platform}
import suzaku.ui.UIProtocol._
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.resource.{EmbeddedResource, ResourceRegistration}
import suzaku.ui.style._
import suzaku.util.DenseIntMap

import scala.collection.mutable

abstract class UIManager(logger: Logger, platform: Platform)
    extends MessageChannelHandler[UIProtocol.type]
    with WidgetParent
    with ColorProvider {
  import UIManager._

  case class RegisteredStyle(id: Int,
                             name: String,
                             props: List[StyleBaseProperty],
                             inherited: List[Int],
                             remaps: DenseIntMap[List[Int]],
                             widgetClasses: DenseIntMap[List[Int]])

  private var widgetClassMap        = DenseIntMap.empty[String]
  private var registeredWidgets     = Map.empty[String, WidgetBuilder[_ <: WidgetProtocol]]
  private var builders              = DenseIntMap.empty[WidgetBuilder[_ <: WidgetProtocol]]
  private var uiChannel: UIChannel  = _
  protected val nodes               = mutable.LongMap[WidgetNode](-1L -> WidgetNode(emptyWidget(-1), Nil, -1))
  protected var rootNode            = Option.empty[WidgetNode]
  protected var registeredStyles    = DenseIntMap.empty[RegisteredStyle]
  protected var registeredResources = DenseIntMap.empty[ResourceRegistration]
  protected var themes              = Vector.empty[(Int, Map[Int, List[Int]])]
  protected var activeTheme         = DenseIntMap.empty[List[Int]]
  protected var activePalette       = Palette.default
  protected var frameRequested      = false
  protected var frameComplete       = true

  override def establishing(channel: MessageChannel[ChannelProtocol]) =
    uiChannel = channel

  def registerWidget(id: String, builder: WidgetBuilder[_ <: WidgetProtocol]): Unit =
    registeredWidgets += id -> builder

  def registerWidget(clazz: Class[_], builder: WidgetBuilder[_ <: WidgetProtocol]): Unit =
    registeredWidgets += clazz.getName -> builder

  def buildWidget(widgetClass: Int,
                  widgetId: Int,
                  channelId: Int,
                  globalId: Int,
                  channelReader: ChannelReader): Option[Widget] = {
    val builder = builders.get(widgetClass) orElse {
      // update builder map from registered widgets
      val b = widgetClassMap.get(widgetClass).flatMap(registeredWidgets.get)
      b.foreach(v => builders = builders.updated(widgetClass -> v))
      b
    }
    builder.map(builder => builder.materialize(widgetId, widgetClass, channelId, globalId, uiChannel, channelReader))
  }

  def shouldRenderFrame: Boolean = frameRequested

  def isFrameComplete: Boolean = frameComplete

  def nextFrame(time: Long): Unit = {
    frameComplete = false
    frameRequested = false
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

  def getStyle(styleId: Int): Option[RegisteredStyle] =
    registeredStyles.get(styleId)

  def getResource(resourceId: Int): Option[EmbeddedResource] =
    registeredResources.get(resourceId).map(_.resource)

  def palette(idx: Int) = activePalette(idx)

  private def setParent(node: WidgetNode, parent: WidgetParent): Unit = {
    // only set parent if the parent has a parent
    if (parent.hasParent) {
      node.widget.setParent(parent)
      node.children.foreach(c => setParent(nodes(c), node.widget))
    }
  }

  private def rebuildThemes(themes: Seq[(Int, Map[Int, List[Int]])]): Unit = {
    // join themes to form the active theme
    activeTheme = themes.foldLeft(DenseIntMap.empty[List[Int]]) {
      case (act, (_, styleMap)) =>
        styleMap.foldLeft(act) {
          case (current, (widgetClassId, styleClasses)) =>
            current.updated(widgetClassId, (current.getOrElse(widgetClassId, Nil) ++ styleClasses).distinct)
        }
    }
    // reapply styles as theme changes may affect them
    rootNode.foreach(n => reapplyStyles(n.widget.widgetId))
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

    case FrameComplete =>
      frameComplete = true

    case SetChildren(widgetId, children) =>
      logger.debug(s"Setting [$children] as children of [$widgetId]")
      val lb = new mutable.ListBuffer[WidgetNode]
      children.foreach(c => lb += nodes(c))
      val childNodes = lb
      nodes.get(widgetId).foreach { node =>
        node.widget.setChildren(childNodes.map(_.widget).asInstanceOf[Seq[node.widget.W]])
        childNodes.foreach(c => setParent(c, node.widget))
        nodes.update(widgetId, node.copy(children = children))
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
        nodes.update(widgetId, node.copy(children = cur))
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
          val remaps = props
            .collect {
              case remap: RemapClasses =>
                var m = DenseIntMap.empty[List[Int]]
                remap.styleMap.foreach { case (key, value) => m = m.updated(key.id, value.map(_.id)) }
                m
            }
            .foldLeft(DenseIntMap.empty[List[Int]])(_ join _)
          val widgetClasses = props
            .collect {
              case widgetClass: WidgetStyles =>
                var m = DenseIntMap.empty[List[Int]]
                widgetClass.styleMapping.foreach { case (key, value) => m = m.updated(key, value.map(_.id)) }
                m
            }
            .foldLeft(DenseIntMap.empty[List[Int]])(_ join _)

          val extProps = extend.flatMap(_.styles.flatMap(sc => registeredStyles(sc.id).props))
          val inherited = if (inherits.nonEmpty) {
            dirtyStyles = true
            val resolved = inherits.flatMap(_.styles.flatMap(s => registeredStyles(s.id).inherited)).distinct
            resolved :+ styleId
          } else styleId :: Nil
          val allProps = extProps ::: baseProps
          val regStyle = RegisteredStyle(styleId, styleName, allProps, inherited, remaps, widgetClasses)
          registeredStyles = registeredStyles.updated(styleId, regStyle)

          regStyle
      }
      (dirtyStyles, rootNode) match {
        case (true, Some(node)) =>
          // set parent recursively to apply changed styles
          setParent(node, this)
        case _ => // nothing to update
      }
      addStyles(baseStyles)

    case AddResources(resources) =>
      resources.foreach { resource =>
        registeredResources = registeredResources.updated(resource.id, resource)
      }
      addEmbeddedResources(resources)

    case AddLayoutIds(ids)       =>
    // TODO store somewhere

    case ActivateTheme(themeId, theme) =>
      themes :+= (themeId, theme)
      rebuildThemes(themes)

    case DeactivateTheme(themeId) =>
      themes = themes.filterNot(_._1 == themeId)
      rebuildThemes(themes)

    case RegisterWidgetClass(className, classId) =>
      logger.debug(s"Register widget class $className as $classId")
      widgetClassMap = widgetClassMap.updated(classId -> className)

    case SetPalette(palette) =>
      activePalette = palette
      resetStyles()
      addStyles(registeredStyles.values)
  }

  override def materializeChildChannel(channelId: Int,
                                       globalId: Int,
                                       parent: MessageChannelBase,
                                       channelReader: ChannelReader): MessageChannelBase = {
    import boopickle.Default._
    // read the component creation data
    val CreateWidget(widgetClass, widgetId) = channelReader.read[CreateWidget]
    logger.debug(f"Building widget ${widgetClassMap(widgetClass)} on channel [$channelId, $globalId%08x]")
    try {
      buildWidget(widgetClass, widgetId, channelId, globalId, channelReader) match {
        case Some(widget) =>
          // add a node for the component
          nodes.update(widgetId, WidgetNode(widget, Vector.empty, channelId))
          widget.channel
        case None =>
          throw new IllegalAccessException(s"Unable to materialize a widget '${widgetClassMap(widgetClass)}'")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Unhandled exception while building widget ${widgetClassMap(widgetClass)}: $e")
        throw e
    }
  }

  override def channelWillClose(id: Int): Unit = {
    logger.debug(s"Widget [$id] removed")
    nodes.remove(id).foreach(node => destroyWidget(node))
  }

  override def hasParent: Boolean =
    true

  override def resolveStyleMapping(wClsId: Int, ids: List[Int]): List[Int] =
    activeTheme.getOrElse(wClsId, Nil) ::: ids

  override def resolveStyleInheritance(ids: List[Int]): List[Int] =
    ids.flatMap(id => registeredStyles(id).inherited)

  override def resolveLayout(widget: Widget, layoutProperties: List[LayoutProperty]): Unit = {}

  override def getStyleMapping: DenseIntMap[List[Int]] =
    DenseIntMap.empty

  override def getWidgetStyleMapping: DenseIntMap[List[Int]] =
    activeTheme

  override def getColor(idx: Int): PaletteEntry =
    activePalette(idx)

  protected def emptyWidget(widgetId: Int): Widget

  protected def destroyWidget(node: WidgetNode): Unit

  protected def mountRoot(node: WidgetArtifact): Unit

  protected def addStyles(styles: Seq[RegisteredStyle]): Unit = {}

  protected def resetStyles(): Unit = {}

  protected def addEmbeddedResources(resources: Seq[ResourceRegistration]): Unit = {}
}

object UIManager {
  case class WidgetNode(widget: Widget, children: Seq[Int], channelId: Int)
}
