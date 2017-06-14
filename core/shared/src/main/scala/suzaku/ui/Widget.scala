package suzaku.ui

import arteria.core._
import suzaku.ui.UIProtocol.ChildOp
import suzaku.ui.WidgetProtocol.{UpdateLayout, UpdateStyle}
import suzaku.ui.layout.LayoutProperty
import suzaku.ui.style.{RemapClasses, StyleBaseProperty, StyleClasses}

abstract class Widget(val widgetId: Int, widgetManager: WidgetManager) extends WidgetParent {
  type CP <: Protocol
  type Artifact <: WidgetArtifact
  type W <: Widget

  protected var parent       = Option.empty[WidgetParent]
  private var messageChannel = null: MessageChannel[CP]
  private var widgetClassId  = -1
  protected var styleClasses = List.empty[Int]
  protected var styleMapping = Map.empty[Int, List[Int]]
  protected var layoutProps  = List.empty[LayoutProperty]

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
    this.parent = Some(parent)
    // reapply styles as mappings might have changed
    reapplyStyles()
  }

  def reapplyStyles(): Unit = {
    val themeClasses = widgetManager.applyTheme(widgetClassId)
    applyStyleClasses(resolveStyle(themeClasses ::: styleClasses))
  }

  def resolveStyle(ids: List[Int]): List[Int] = {
    resolveStyleMapping(resolveStyleInheritance(ids))
  }

  override def resolveStyleMapping(ids: List[Int]): List[Int] = {
    val sid = ids.flatMap(id => styleMapping.getOrElse(id, id :: Nil))
    parent.map(_.resolveStyleMapping(sid)).getOrElse(sid)
  }

  override def resolveStyleInheritance(ids: List[Int]): List[Int] =
    parent.map(_.resolveStyleInheritance(ids)).getOrElse(ids)

  override def resolveLayout(widget: Widget, layoutProperties: List[LayoutProperty]): Unit = {}
}

trait WidgetParent {
  def resolveStyleMapping(id: List[Int]): List[Int]

  def resolveStyleInheritance(id: List[Int]): List[Int]

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
          styleMapping =
            if (remove)
              Map.empty
            else
              styleMap.map { case (style, mappedTo) => (style.id, resolveStyleInheritance(mappedTo.map(_.id))) }
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
