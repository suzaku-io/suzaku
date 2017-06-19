package suzaku.ui

import arteria.core._
import suzaku.platform.Logger
import suzaku.ui.UIProtocol._
import suzaku.ui.layout.LayoutIdRegistry
import suzaku.ui.style.{StyleClassRegistry, Theme}

import scala.collection.mutable
import scala.collection.immutable

class UIManager(logger: Logger, channelEstablished: UIChannel => Unit, flushMessages: () => Unit)
    extends MessageChannelHandler[UIProtocol.type] {
  import UIManager._

  private var lastFrame        = 0L
  protected var uiChannel      = null: MessageChannel[ChannelProtocol]
  private var currentRoot      = Option.empty[ShadowNode]
  private[suzaku] var widgetId = 1
  private var dirtyRoots       = List.empty[ShadowComponent]
  private var frameRequested   = false
  private var themeId          = 0

  def render(root: Blueprint): Unit = {
    val newRoot = updateBranch(currentRoot, root, None)
    currentRoot match {
      case Some(widget) if widget.getId == newRoot.getId =>
      // no-op
      case Some(widget) =>
        logger.debug(s"Replacing root [${widget.getId}] with [${newRoot.getId}]")
        widget.destroy()
        send(MountRoot(newRoot.getId))
      case None =>
        logger.debug(s"Mounting [${newRoot.getId}] as root")
        send(MountRoot(newRoot.getId))
    }
    // check if styles have updated
    if (StyleClassRegistry.hasRegistrations) {
      val styles = StyleClassRegistry.dequeueRegistrations
      logger.debug(s"Adding ${styles.size} styles")
      send(AddStyles(styles))
    }
    // check if layout identifiers updated
    if (LayoutIdRegistry.hasRegistrations) {
      val layoutIds = LayoutIdRegistry.dequeueRegistrations
      logger.debug(s"Adding ${layoutIds.size} layout identifiers")
      send(AddLayoutIds(layoutIds))
    }
    currentRoot = Some(newRoot)
  }

  def activateTheme(theme: Theme): Int = {
    val id = themeId
    themeId += 1
    val activateTheme = ActivateTheme(
      id,
      theme.styleMap
        .map {
          case (widgetClass, styleClasses) =>
            UIManager.getWidgetClass(widgetClass.blueprintClass, uiChannel) -> styleClasses.map(_.id)
        }
    )
    send(activateTheme)
    id
  }

  def deactivateTheme(themeId: Int): Unit = {
    send(DeactivateTheme(themeId))
  }

  @inline final protected def send[A <: Message](message: A)(implicit ev: MessageWitness[A, ChannelProtocol]) = {
    uiChannel.send(message)
  }

  override def process = {
    case NextFrame(time) =>
      lastFrame = time
      frameRequested = false
      // update dirty component trees
      if (dirtyRoots.nonEmpty)
        logger.debug(s"Updating ${dirtyRoots.size} dirty components")

      dirtyRoots.foreach { shadowComponent =>
        updateBranch(Some(shadowComponent), shadowComponent.blueprint, shadowComponent.parent)
      }
      dirtyRoots = Nil
      // check if styles have updated
      if (StyleClassRegistry.hasRegistrations) {
        val styles = StyleClassRegistry.dequeueRegistrations
        logger.debug(s"Adding ${styles.size} styles")
        send(AddStyles(styles))
      }
  }

  override def established(channel: MessageChannel[ChannelProtocol]) = {
    uiChannel = channel
    channelEstablished(channel)
  }

  private def addDirtyRoot(component: ShadowComponent): Unit = {
    dirtyRoots ::= component
    // request a new frame to redraw UI
    if (!frameRequested) {
      frameRequested = true
      send(RequestFrame)
      flushMessages()
    }
  }

  private def expand(node: ShadowNode): immutable.Seq[ShadowNode] = node match {
    case seq: ShadowNodeSeq =>
      seq.children.flatMap(expand)
    case _ =>
      List(node)
  }

  private def expandBP(bp: Blueprint): immutable.Seq[Blueprint] = bp match {
    case BlueprintSeq(blueprints) =>
      blueprints.flatMap(expandBP)
    case _ =>
      List(bp)
  }

  private def allocateId: Int = {
    val id = widgetId
    widgetId += 1
    id
  }

  private def updateBranch(current: Option[ShadowNode], blueprint: Blueprint, parent: Option[ShadowNode]): ShadowNode = {
    current match {
      case None =>
        // nothing to compare to, just keep rendering
        blueprint match {
          case EmptyBlueprint =>
            EmptyNode

          case widgetBP: WidgetBlueprint =>
            val widgetId   = allocateId
            val shadowWidget = new ShadowWidget(widgetBP, widgetId, parent, uiChannel)
            if (widgetBP.children.nonEmpty) {
              val children = widgetBP.children.map(c => updateBranch(None, c, Some(shadowWidget)))
              send(SetChildren(widgetId, children.flatMap(expand).map(_.getId)))
              shadowWidget.withChildren(children)
            } else {
              shadowWidget
            }

          case componentBP: ComponentBlueprint =>
            val shadowComponent =
              new ShadowComponent(componentBP, rendered => updateBranch(None, rendered, parent), parent, addDirtyRoot)
            shadowComponent

          case BlueprintSeq(blueprints) =>
            val shadowSeq = new ShadowNodeSeq(blueprints, parent)
            shadowSeq.withChildren(blueprints.map(bp => updateBranch(None, bp, Some(shadowSeq))))

        }

      case Some(EmptyNode) =>
        // there's an empty node here that should be replaced
        blueprint match {
          case EmptyBlueprint =>
            EmptyNode

          case _ =>
            updateBranch(None, blueprint, parent)
        }

      case Some(node: ShadowWidget) =>
        // there's an existing widget node here that should be updated/replaced
        blueprint match {
          case EmptyBlueprint =>
            EmptyNode

          case widgetBP: WidgetBlueprint if widgetBP.getClass eq node.blueprint.getClass =>
            val (newChildren, ops) = updateChildren(node, node.children.flatMap(expand), widgetBP.children.flatMap(expandBP))
            if (ops.nonEmpty)
              send(UpdateChildren(node.widgetId, ops))
            node.children = newChildren
            if (widgetBP sameAs node.blueprint.asInstanceOf[widgetBP.This]) {
              // no change, return current node
              node
            } else {
              // update node
              node.proxy.update(widgetBP)
              node.blueprint = widgetBP
              node
            }

          case _ =>
            // always replace when different widget or component
            updateBranch(None, blueprint, parent)
        }

      case Some(node: ShadowComponent) =>
        blueprint match {
          case EmptyBlueprint =>
            EmptyNode
          case nextBlueprint: ComponentBlueprint if nextBlueprint.getClass eq node.blueprint.getClass =>
            val bpChange = !(nextBlueprint sameAs node.blueprint.asInstanceOf[nextBlueprint.type])
            if (bpChange) {
              node.component.willReceiveBlueprint(nextBlueprint)
            }
            if (node.isDirty || bpChange) {
              if (node.component.shouldUpdate(nextBlueprint, node.state, node.nextState)) {
                node.blueprint = nextBlueprint
                node.component._blueprint = nextBlueprint
                node.rendered = node.component.render(node.nextState)
                node.inner = updateBranch(Some(node.inner), node.rendered, parent)
                node.component.didUpdate(nextBlueprint, node.nextState)
              }
              node.isDirty = false
              node.state = node.nextState
            }
            node
          case _ =>
            // always replace when different widget or component
            updateBranch(None, blueprint, parent)
        }

      case Some(seq: ShadowNodeSeq) =>
        throw new IllegalStateException("ShadowNodeSeq cannot be updated directly")
    }
  }

  def updateChildren(currentNode: ShadowNode,
                     previous: Seq[ShadowNode],
                     next: List[Blueprint]): (Seq[ShadowNode], Seq[ChildOp]) = {
    // extract all keys only if needed
    lazy val prevKeys: mutable.Set[Any] = previous.flatMap(_.key)(collection.breakOut)
    lazy val nextKeys: mutable.Set[Any] = next.flatMap(_.key)(collection.breakOut)
    val newNodes                        = mutable.ArrayBuffer[ShadowNode](previous: _*)

    var pos = 0
    val ops = mutable.ArrayBuffer.empty[ChildOp]

    next.foreach { bp =>
      bp.key match {
        case _ if pos >= newNodes.size =>
          // we are past previous nodes, just insert new
          val newNode = updateBranch(None, bp, Some(currentNode))
          newNodes.append(newNode)
          ops.append(InsertOp(newNode.getId))

        case Some(key) if prevKeys(key) =>
          // find the matching node
          var idx = pos
          while (!newNodes(idx).blueprint.key.contains(key)) {
            idx += 1
          }
          // move and update the found node to current position
          newNodes.insert(pos, updateBranch(Some(newNodes.remove(idx)), bp, Some(currentNode)))
          if (pos == idx)
            ops.append(NoOp())
          else
            ops.append(MoveOp(idx))

        case _ =>
          // if current node has a key that is still upcoming, do not replace but insert
          val currentNode = newNodes(pos)
          if (currentNode.blueprint.key.exists(nextKeys.contains)) {
            val newNode = updateBranch(None, bp, Some(currentNode))
            newNodes.insert(pos, newNode)
            ops.append(InsertOp(newNode.getId))
          } else {
            val newNode = updateBranch(Some(currentNode), bp, Some(currentNode))
            if (newNode eq currentNode) {
              ops.append(NoOp())
            } else {
              newNodes(pos).destroy()
              newNodes(pos) = newNode
              ops.append(ReplaceOp(newNode.getId))
            }
          }
      }
      bp.key.foreach { key =>
        prevKeys.remove(key)
        nextKeys.remove(key)
      }
      pos += 1
    }
    // check if there are excess nodes left over
    if (pos < newNodes.size) {
      ops.append(RemoveOp(newNodes.size - pos))
      newNodes.drop(pos).foreach(_.destroy())
      newNodes.remove(pos, newNodes.size - pos)
    }
    // compress NoOps
    val finalOps: Seq[ChildOp] = if (ops.size > 1) {
      val compressed = ops.tail.foldLeft(mutable.ArrayBuffer[ChildOp](ops.head)) { (res, op) =>
        (res.last, op) match {
          case (NoOp(a), NoOp(b)) =>
            res(res.size - 1) = NoOp(a + b)
            res
          case _ =>
            res.append(op)
            res
        }
      }
      if (compressed.last.isInstanceOf[NoOp])
        compressed.init
      else
        compressed
    } else {
      ops.filterNot(_.isInstanceOf[NoOp])
    }
    (newNodes, finalOps)
  }
}

object UIManager {

  sealed abstract class ShadowNode(val parent: Option[ShadowNode]) {
    type BP <: Blueprint
    var blueprint: BP

    def getWidget: ShadowWidget

    def getId: Int = getWidget.widgetId

    def key: Iterable[Any] = blueprint.key

    def destroy(): Unit
  }

  private[suzaku] final object EmptyNode extends ShadowNode(None) {
    type BP = EmptyBlueprint.type

    override var blueprint             = EmptyBlueprint
    override def getWidget: ShadowWidget = null
    override def getId: Int            = -1
    override def destroy(): Unit       = {}
  }

  private[suzaku] final class ShadowNodeSeq(blueprints: List[Blueprint], parent: Option[ShadowNode])
      extends ShadowNode(parent) {
    type BP = EmptyBlueprint.type

    override var blueprint = EmptyBlueprint
    var children           = List.empty[ShadowNode]

    def withChildren(c: List[ShadowNode]): ShadowNodeSeq = {
      children = c
      this
    }

    override def getWidget: ShadowWidget = throw new NotImplementedError("ShadowNodeSeq does not have a widget")

    override def getId: Int = throw new NotImplementedError("ShadowNodeSeq does not have an id")

    override def key: Iterable[Any] = blueprints.flatMap(_.key)

    override def destroy(): Unit =
      children.foreach(_.destroy())

    override def toString: String = s"ShadowNodeSeq($blueprints)"
  }

  private[suzaku] final class ShadowWidget(var blueprint: WidgetBlueprint,
                                           val widgetId: Int,
                                           parent: Option[ShadowNode],
                                           uiChannel: UIChannel)
      extends ShadowNode(parent) {
    type BP = WidgetBlueprint

    val proxy    = blueprint.createProxy(widgetId, uiChannel).asInstanceOf[WidgetProxy[Protocol, WidgetBlueprint]]
    var children = Seq.empty[ShadowNode]

    override def getWidget: ShadowWidget = this

    override def destroy(): Unit = {
      children.foreach(_.destroy())
      proxy.destroyWidget()
    }

    def withChildren(c: List[ShadowNode]): ShadowWidget = {
      children = c
      this
    }

    override def toString: String = s"ShadowWidget($blueprint, $widgetId)"
  }

  private[suzaku] final class ShadowComponent(var blueprint: ComponentBlueprint,
                                              innerBuilder: Blueprint => ShadowNode,
                                              parent: Option[ShadowNode],
                                              addDirtyRoot: ShadowComponent => Unit)
      extends ShadowNode(parent)
      with StateProxy {
    type BP = ComponentBlueprint

    val component = blueprint.create(this).asInstanceOf[Component[ComponentBlueprint, Any]]
    var state     = component.initialState
    var rendered  = component.render(state)
    var inner     = innerBuilder(rendered)
    var isDirty   = false
    var nextState = state

    component.didMount()

    override def getWidget: ShadowWidget = inner.getWidget

    override def modState[S](f: (S) => S): Unit = {
      nextState = f(nextState.asInstanceOf[S])
      if (!isDirty) {
        addDirtyRoot(this)
        isDirty = true
      }
    }

    override def destroy(): Unit = {
      inner.destroy()
      component.didUnmount()
    }

    override def toString: String = s"ShadowComponent($blueprint)"
  }

  private var widgetClasses = Map.empty[String, Int]
  private var widgetClassId = 0

  def getWidgetClass(blueprint: Class[_ <: WidgetBlueprint], uiChannel: MessageChannel[UIProtocol.type]): Int = {
    this.synchronized {
      val name = blueprint.getName
      widgetClasses.get(name) match {
        case Some(id) => id
        case None     =>
          // create integer mapping and register the class
          val id = widgetClassId
          widgetClassId += 1
          widgetClasses += name -> id
          uiChannel.send(RegisterWidgetClass(name, id))
          id
      }
    }
  }
}
