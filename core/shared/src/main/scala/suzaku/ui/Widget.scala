package suzaku.ui

import arteria.core._
import suzaku.ui.UIProtocol.ChildOp
import suzaku.ui.WidgetProtocol.UpdateStyle
import suzaku.ui.style.{RemapClasses, StyleBaseProperty, StyleClasses}

abstract class Widget(val widgetId: Int, widgetManager: WidgetManager) extends WidgetParent {
  type CP <: Protocol
  type Artifact <: WidgetArtifact
  type W <: Widget

  private var parent         = Option.empty[WidgetParent]
  private var messageChannel = null: MessageChannel[CP]
  protected var styleClasses = List.empty[Int]
  protected var styleMapping = Map.empty[Int, List[Int]]

  protected[suzaku] def withChannel(channel: MessageChannel[CP]): this.type = {
    messageChannel = channel
    this
  }

  def channel: MessageChannel[CP] = messageChannel

  def artifact: Artifact

  def setChildren(children: Seq[W]): Unit =
    throw new NotImplementedError("This widget cannot have children")

  def updateChildren(ops: Seq[ChildOp], widget: Int => W): Unit

  def applyStyleClasses(styles: List[Int]): Unit

  def applyStyleProperty(prop: StyleBaseProperty, remove: Boolean): Unit

  def setParent(parent: WidgetParent): Unit = {
    this.parent = Some(parent)
    // reapply styles as mappings might have changed
    applyStyleClasses(styleClasses.flatMap(resolveStyle))
  }

  def reapplyStyles(): Unit = {
    applyStyleClasses(styleClasses.flatMap(resolveStyle))
  }

  def resolveStyle(id: Int): List[Int] = {
    resolveStyleInheritance(id).flatMap(resolveStyleMapping)
  }

  override def resolveStyleMapping(id: Int): List[Int] = {
    styleMapping
      .getOrElse(id, id :: Nil)
      .flatMap(sid => parent.map(_.resolveStyleMapping(sid)).getOrElse(sid :: Nil))
  }

  override def resolveStyleInheritance(id: Int): List[Int] = parent match {
    case Some(parentWidget) => parentWidget.resolveStyleInheritance(id)
    case None               => id :: Nil
  }
}

trait WidgetParent {
  def resolveStyleMapping(id: Int): List[Int]

  def resolveStyleInheritance(id: Int): List[Int]
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
          applyStyleClasses(styleClasses.flatMap(resolveStyle))
        case (RemapClasses(styleMap), remove) =>
          // println(s"Remapping styles: $styleMap (remove = $remove")
          styleMapping =
            if (remove)
              Map.empty
            else
              styleMap.map { case (style, mappedTo) => (style.id, mappedTo.map(_.id)) }
          // reapply styles to children as mappings might have changed
          widgetManager.reapplyStyles(widgetId)
        case (prop: StyleBaseProperty, remove) =>
          applyStyleProperty(prop, remove)
        case unknown =>
          throw new IllegalArgumentException(s"Style property '$unknown' not allowed here")
      }
  }
}

abstract class WidgetBuilder[P <: Protocol](protocol: P) {
  protected def create(widgetId: Int, context: P#ChannelContext): WidgetWithProtocol[P]

  def materialize(widgetId: Int,
                  channelId: Int,
                  globalId: Int,
                  parent: MessageChannelBase,
                  channelReader: ChannelReader): Widget = {
    val context = channelReader.read(protocol.contextPickler)
    val widget  = create(widgetId, context)
    val channel = new MessageChannel(protocol)(channelId, globalId, parent, widget, context)
    widget.withChannel(channel)
  }
}
