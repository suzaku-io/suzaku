package suzaku.ui

import arteria.core._
import suzaku.ui.UIProtocol.ChildOp
import suzaku.ui.WidgetProtocol.UpdateStyle
import suzaku.ui.style.{StyleBaseProperty, StyleClasses}

abstract class Widget extends WidgetParent {
  type CP <: Protocol
  type Artifact <: WidgetArtifact
  type W <: Widget

  private var parent         = Option.empty[WidgetParent]
  private var messageChannel = null: MessageChannel[CP]
  protected var styleClasses = List.empty[Int]

  protected[suzaku] def withChannel(channel: MessageChannel[CP]): this.type = {
    messageChannel = channel
    this
  }

  def channel: MessageChannel[CP] = messageChannel

  def artifact: Artifact

  def setChildren(children: Seq[W]): Unit =
    throw new NotImplementedError("This widget cannot have children")

  def updateChildren(ops: Seq[ChildOp], widget: Int => W): Unit

  def setParent(parent: WidgetParent): Unit = {
    println(s"Setting parent for $this")
    this.parent = Some(parent)
    applyStyleClasses(styleClasses.flatMap(mapStyle))
  }

  def applyStyleClasses(styles: List[Int]): Unit

  def applyStyleProperty(prop: StyleBaseProperty, remove: Boolean): Unit

  override def mapStyle(id: Int): List[Int] = parent match {
    case Some(parentWidget) => parentWidget.mapStyle(id)
    case None               => List(id)
  }
}

trait WidgetParent {
  def mapStyle(id: Int): List[Int]
}

abstract class WidgetArtifact

abstract class WidgetWithProtocol[P <: Protocol] extends Widget with MessageChannelHandler[P] {
  override type CP = P

  override def process = {
    case UpdateStyle(props) =>
      props.foreach {
        case (StyleClasses(styles), remove) =>
          styleClasses = if(remove) Nil else styles.map(_.id)
          applyStyleClasses(styleClasses.flatMap(mapStyle))
        case (prop: StyleBaseProperty, remove) =>
          applyStyleProperty(prop, remove)
        case unknown =>
          throw new IllegalArgumentException(s"Style property '$unknown' not allowed here")
      }
  }
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
