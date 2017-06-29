package suzaku.ui

import arteria.core._
import suzaku.ui.UIProtocol.ChildOp
import suzaku.ui.WidgetProtocol.{UpdateLayout, UpdateStyle}
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.{RemapClasses, StyleBaseProperty, StyleClasses, WidgetStyles}

import scala.collection.immutable.IntMap

abstract class Widget(val widgetId: Int, widgetManager: WidgetManager) extends WidgetParent {
  type CP <: Protocol
  type Artifact <: WidgetArtifact
  type W <: Widget

  protected var parent                   = Option.empty[WidgetParent]
  private var messageChannel             = null: MessageChannel[CP]
  private var widgetClassId              = -1
  protected var styleClasses             = List.empty[Int]
  protected var directStyleMapping       = IntMap.empty[List[Int]]
  protected var parentStyleMapping       = IntMap.empty[List[Int]]
  protected var styleMapping             = IntMap.empty[List[Int]]
  protected var directWidgetStyleMapping = IntMap.empty[List[Int]]
  protected var parentWidgetStyleMapping = IntMap.empty[List[Int]]
  protected var widgetStyleMapping       = IntMap.empty[List[Int]]
  protected var layoutProps              = List.empty[LayoutProperty]

  protected[suzaku] def withChannel(channel: MessageChannel[CP]): this.type = {
    messageChannel = channel
    this
  }

  protected[suzaku] def withClass(classId: Int): this.type = {
    widgetClassId = classId
    this
  }

  def channel: MessageChannel[CP] = messageChannel

  def artifact: Artifact

  def setChildren(children: Seq[Widget]): Unit =
    throw new NotImplementedError("This widget cannot have children")

  def updateChildren(ops: Seq[ChildOp], widget: Int => W): Unit

  def applyStyleClasses(styles: List[Int]): Unit

  def applyStyleProperty(prop: StyleBaseProperty, remove: Boolean): Unit

  def widgetClass: Int = widgetClassId

  def layoutProperties = layoutProps

  def setParent(parent: WidgetParent): Unit = {
    // println(s"Setting parent of $getClass to ${parent.getClass}")
    this.parent = Some(parent)
    parentWidgetStyleMapping = parent.getWidgetStyleMapping
    parentStyleMapping = parent.getStyleMapping
    // reapply styles as mappings might have changed
    reapplyStyles()
  }

  def reapplyStyles(): Unit = {
    // only apply styles if parent is defined
    if (hasParent)
      applyStyleClasses(resolveStyle(styleClasses))
  }

  def resolveStyle(styleIds: List[Int]): List[Int] = {
    // println(s"Resolving styles for $getClass")
    // get styles that are assigned to this widget class
    val widgetMapping =
      parentWidgetStyleMapping.getOrElse(widgetClassId, Nil) ::: directWidgetStyleMapping.getOrElse(widgetClassId, Nil)
    val inheritedStyles = if (widgetMapping.nonEmpty || styleIds.nonEmpty) {
      // remap styles
      val remapping =
        (widgetMapping ::: styleIds)
          .flatMap(id => parentStyleMapping.getOrElse(id, id :: Nil))
          .flatMap(id => directStyleMapping.getOrElse(id, id :: Nil))

      remapping.flatMap(id => widgetManager.getStyle(id)).flatMap(_.inherited).distinct
    } else Nil

    val styles = inheritedStyles.flatMap(id => widgetManager.getStyle(id))

    // join all widget style mappings
    widgetStyleMapping =
      styles.foldLeft(joinMaps(parentWidgetStyleMapping, directWidgetStyleMapping))((a, b) => joinMaps(a, b.widgetClasses))
    // join all style remappings
    styleMapping = styles.foldLeft(joinMaps(parentStyleMapping, directStyleMapping))((a, b) => joinMaps(a, b.remaps))
    inheritedStyles
  }

  private def joinMaps(map1: IntMap[List[Int]], map2: IntMap[List[Int]]): IntMap[List[Int]] = {
    if (map1.isEmpty)
      map2
    else if (map2.isEmpty)
      map1
    else
      map2.foldLeft(map1) {
        case (map, (key, value)) => map.updated(key, if (map.contains(key)) map(key) ::: value else value)
      }
  }

  override def hasParent: Boolean = parent.isDefined

  override def resolveStyleMapping(wClsId: Int, styles: List[Int]): List[Int] = {
    val mappedStyles = styles.flatMap(id => directStyleMapping.getOrElse(id, id :: Nil))
    parent.map(_.resolveStyleMapping(wClsId, mappedStyles)).getOrElse(mappedStyles)
  }

  override def resolveStyleInheritance(ids: List[Int]): List[Int] =
    parent.map(_.resolveStyleInheritance(ids)).getOrElse(ids)

  override def resolveLayout(widget: Widget, layoutProperties: List[LayoutProperty]): Unit = {}

  override def getStyleMapping: IntMap[List[Int]] = styleMapping

  override def getWidgetStyleMapping: IntMap[List[Int]] = widgetStyleMapping
}

trait WidgetParent {
  def hasParent: Boolean

  def getStyleMapping: IntMap[List[Int]]

  def getWidgetStyleMapping: IntMap[List[Int]]

  def resolveStyleMapping(wClsId: Int, styles: List[Int]): List[Int]

  def resolveStyleInheritance(styles: List[Int]): List[Int]

  def resolveLayout(widget: Widget, layoutProperties: List[LayoutProperty]): Unit
}

abstract class WidgetArtifact

abstract class WidgetWithProtocol[P <: Protocol](widgetId: Int, widgetManager: WidgetManager)
    extends Widget(widgetId, widgetManager)
    with MessageChannelHandler[P] {
  override type CP = P

  override def process = {
    case UpdateStyle(props) =>
      props.foreach {
        case (StyleClasses(styles), remove) =>
          styleClasses = if (remove) Nil else styles.map(_.id)
          reapplyStyles()
        case (RemapClasses(styleMap), remove) =>
          // println(s"Remapping styles: $styleMap (remove = $remove")
          directStyleMapping =
            if (remove)
              IntMap.empty
            else
              styleMap.map {
                case (style, mappedTo) => (style.id, resolveStyleInheritance(mappedTo.map(_.id)))
              }(collection.breakOut): IntMap[List[Int]]
          // reapply styles to children as mappings might have changed
          widgetManager.reapplyStyles(widgetId)
        case (WidgetStyles(styleMap), remove) =>
          // println(s"Remapping styles: $styleMap (remove = $remove")
          directWidgetStyleMapping =
            if (remove)
              IntMap.empty
            else
              styleMap.map {
                case (wClsId, mappedTo) => (wClsId, resolveStyleInheritance(mappedTo.map(_.id)))
              }(collection.breakOut): IntMap[List[Int]]
          // reapply styles to children as mappings might have changed
          widgetManager.reapplyStyles(widgetId)
        case (prop: StyleBaseProperty, remove) =>
          applyStyleProperty(prop, remove)
        case unknown =>
          throw new IllegalArgumentException(s"Style property '$unknown' not allowed here")
      }

    case UpdateLayout(props) =>
      println(s"Update layout with $props")
      layoutProps = props
      parent.foreach(_.resolveLayout(this, layoutProps))
  }
}

abstract class WidgetBuilder[P <: Protocol](protocol: P) {
  protected def create(widgetId: Int, context: P#ChannelContext): WidgetWithProtocol[P]

  def materialize(widgetId: Int,
                  widgetClass: Int,
                  channelId: Int,
                  globalId: Int,
                  parent: MessageChannelBase,
                  channelReader: ChannelReader): Widget = {
    val context = channelReader.read(protocol.contextPickler)
    val widget  = create(widgetId, context)
    val channel = new MessageChannel(protocol)(channelId, globalId, parent, widget, context)
    widget.withChannel(channel).withClass(widgetClass)
  }
}
