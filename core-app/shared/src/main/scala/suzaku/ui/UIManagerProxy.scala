package suzaku.ui

import arteria.core._
import suzaku.platform.Logger
import suzaku.ui.UIProtocol._
import suzaku.ui.layout.LayoutIdRegistry
import suzaku.ui.resource.EmbeddedResourceRegistry
import suzaku.ui.style.{Palette, StyleClassRegistry, Theme}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class UIManagerProxy(logger: Logger, channelEstablished: UIChannel => Unit, flushMessages: () => Unit)
    extends MessageChannelHandler[UIProtocol.type] {
  import UIManagerProxy._

  private[suzaku] var widgetId = 1
  private var lastFrame        = 0L
  private var currentRoot      = Option.empty[ShadowNode]
  private var dirtyRoots       = List.empty[ShadowComponent]
  private var frameRequested   = false
  private var themeId          = 0
  private val stateModQueue    = mutable.ArrayBuffer.empty[(ShadowComponent, () => Unit)]
  @volatile
  private var isRendering = false

  protected var uiChannel = null: MessageChannel[ChannelProtocol]

  // get notification when styles have updated
  StyleClassRegistry.onRegistration(() => {
    val styles = StyleClassRegistry.dequeueRegistrations
    logger.debug(s"Adding ${styles.size} styles")
    send(AddStyles(styles))
  })

  // get notification when layout IDs have updated
  LayoutIdRegistry.onRegistration(() => {
    val layoutIds = LayoutIdRegistry.dequeueRegistrations
    logger.debug(s"Adding ${layoutIds.size} layout identifiers")
    send(AddLayoutIds(layoutIds))
  })

  // get notification when embedded resources have updated
  EmbeddedResourceRegistry.onRegistration(() => {
    val resources = EmbeddedResourceRegistry.dequeueRegistrations
    logger.debug(s"Adding ${resources.size} embedded resources")
    send(AddResources(resources))
  })

  def render(root: Blueprint): Unit = {
    isRendering = true
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
    dirtyRoots = Nil
    processStateMods()
    currentRoot = Some(newRoot)
    isRendering = false
  }

  def activateTheme(theme: Theme): Int = {
    val id = themeId
    themeId += 1
    val activateTheme = ActivateTheme(
      id,
      theme.styleMap
        .map {
          case (widgetClass, styleClasses) =>
            UIManagerProxy.getWidgetClass(widgetClass.widgetProtocol) -> styleClasses.map(_.id)
        }
    )
    send(activateTheme)
    id
  }

  def deactivateTheme(themeId: Int): Unit = {
    send(DeactivateTheme(themeId))
  }

  def setPalette(palette: Palette): Unit = {
    send(SetPalette(palette))
  }

  @inline final protected def send[A <: Message](message: A)(implicit ev: MessageWitness[A, ChannelProtocol]) = {
    uiChannel.send(message)
  }

  override def process = {
    case NextFrame(time) =>
      isRendering = true
      lastFrame = time
      // update dirty component trees
      if (dirtyRoots.nonEmpty) {
        //logger.debug(s"Updating ${dirtyRoots.size} dirty components")
      }

      dirtyRoots.foreach { shadowComponent =>
        updateBranch(Some(shadowComponent), shadowComponent.blueprint, shadowComponent.parent)
      }
      // process state modifications
      processStateMods()
      // send(FrameComplete)
      flushMessages()
      isRendering = false
  }

  override def established(channel: MessageChannel[ChannelProtocol]) = {
    uiChannel = channel
    internalUiChannel = channel
    channelEstablished(channel)
  }

  private def enqueueStateMod(component: ShadowComponent, runStateMod: () => Unit): Unit = {
    stateModQueue.append(component -> runStateMod)
    if (!isRendering && !frameRequested) {
      frameRequested = true
      send(RequestFrame)
      flushMessages()
    }
  }

  @tailrec
  private def tailContains[A](a: List[A], b: List[A]): Boolean = a match {
    case Nil       => false
    case _ :: tail => if (tail eq b) true else tailContains(tail, b)
  }

  private def processStateMods(): Unit = {
    frameRequested = false
    dirtyRoots = Nil
    stateModQueue.foreach {
      case (component, runStateMod) =>
        if (!component.isDestroyed) {
          runStateMod()
          if (!component.isDirty) {
            component.isDirty = true
            // add to dirty roots only if a parent component is not already there
            // remove all roots that have this component as an ancestor
            if (!dirtyRoots.exists(c => tailContains(component.componentId, c.componentId))) {
              dirtyRoots = dirtyRoots.filterNot(c => tailContains(c.componentId, component.componentId))
              dirtyRoots ::= component
            }
            // request a new frame to redraw UI
            frameRequested = true
          }
        }
    }
    if (frameRequested) {
      // send(RequestFrame)
    }
    stateModQueue.clear()
  }

  private def expand(node: ShadowNode, lb: ListBuffer[ShadowNode]): ListBuffer[ShadowNode] = node match {
    case seq: ShadowNodeSeq =>
      seq.children.foreach(c => expand(c, lb))
      lb
    case _ =>
      lb += node
      lb
  }

  private def expandBP(bp: Blueprint, lb: ListBuffer[Blueprint]): ListBuffer[Blueprint] = bp match {
    case BlueprintSeq(blueprints) =>
      blueprints.foreach(bp => expandBP(bp, lb))
      lb
    case _ =>
      lb += bp
      lb
  }

  @inline private def allocateId: Int = {
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
            new EmptyNode(parent)

          case widgetBP: WidgetBlueprint =>
            val widgetId     = allocateId
            val shadowWidget = new ShadowWidget(widgetBP, widgetId, parent, uiChannel)
            if (widgetBP.children.nonEmpty) {
              val children = widgetBP.children.map(c => updateBranch(None, c, Some(shadowWidget)))
              val lb       = ListBuffer.empty[ShadowNode]
              children.foreach(c => expand(c, lb))
              send(SetChildren(widgetId, lb.map(_.getId).toList))
              shadowWidget.withChildren(children)
            } else {
              shadowWidget
            }

          case componentBP: ComponentBlueprint =>
            val shadowComponent =
              new ShadowComponent(componentBP,
                                  (rendered, thisParent) => updateBranch(None, rendered, thisParent),
                                  parent,
                                  enqueueStateMod)
            shadowComponent

          case BlueprintSeq(blueprints) =>
            val shadowSeq = new ShadowNodeSeq(blueprints, parent)
            shadowSeq.withChildren(blueprints.map(bp => updateBranch(None, bp, Some(shadowSeq))))
        }

      case Some(node: EmptyNode) =>
        // there's an empty node here that should be replaced
        blueprint match {
          case EmptyBlueprint =>
            node

          case _ =>
            updateBranch(None, blueprint, node.parent)
        }

      case Some(node: ShadowWidget) =>
        // there's an existing widget node here that should be updated/replaced
        blueprint match {
          case EmptyBlueprint =>
            new EmptyNode(node.parent)

          case widgetBP: WidgetBlueprint if widgetBP.getClass eq node.blueprint.getClass =>
            val lb   = ListBuffer.empty[ShadowNode]
            val bplb = ListBuffer.empty[Blueprint]
            node.children.foreach(c => expand(c, lb))
            widgetBP.children.foreach(c => expandBP(c, bplb))
            val (newChildren, ops) =
              updateChildren(node, lb.toList, bplb.toList)
            if (ops.nonEmpty)
              send(UpdateChildren(node.widgetId, ops.toList))
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
            new EmptyNode(node.parent)

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
                node.inner = updateBranch(Some(node.inner), node.rendered, Some(node))
                node.component.didUpdate(nextBlueprint, node.nextState)
              }
              node.isDirty = false
              node.state = node.nextState
            }
            node
          case _ =>
            // always replace when different widget or component
            updateBranch(None, blueprint, node.parent)
        }

      case Some(seq: ShadowNodeSeq) =>
        throw new IllegalStateException("ShadowNodeSeq cannot be updated directly")
    }
  }

  def updateChildren(currentNode: ShadowNode,
                     previous: Seq[ShadowNode],
                     next: Seq[Blueprint]): (Seq[ShadowNode], Seq[ChildOp]) = {
    // extract all keys only if needed
    lazy val prevKeys: mutable.Set[Any] = previous.flatMap(_.key)(collection.breakOut)
    lazy val nextKeys: mutable.Set[Any] = next.flatMap(_.key)(collection.breakOut)
    val newNodes                        = mutable.ListBuffer[ShadowNode](previous: _*)

    var pos = 0
    val ops = mutable.ListBuffer.empty[ChildOp]

    next.foreach { bp =>
      bp.key match {
        case _ if pos >= newNodes.size =>
          // we are past previous nodes, just insert new
          val newNode = updateBranch(None, bp, Some(currentNode))
          newNodes += newNode
          ops += InsertOp(newNode.getId)

        case Some(key) if prevKeys.contains(key) =>
          // find the matching node
          var idx         = pos
          var found       = false
          var removeCount = 0
          var removed     = false
          while (!found) {
            newNodes(idx).blueprint.key match {
              case Some(prevKey) if prevKey == key =>
                found = true
                if (removeCount > 0) {
                  ops += RemoveOp(removeCount)
                  newNodes.remove(pos, removeCount)
                  removed = true
                }
              case Some(prevKey) if !nextKeys.contains(prevKey) && removeCount >= 0 =>
                removeCount += 1
                idx += 1
              case _ =>
                idx += 1
                if (removeCount > 0) {
                  ops += RemoveOp(removeCount)
                  pos += removeCount
                  newNodes.remove(pos, removeCount)
                  removed = true
                }
                removeCount = -1
            }
          }
          if (!removed) {
            // move and update the found node to current position
            newNodes.insert(pos, updateBranch(Some(newNodes.remove(idx)), bp, Some(currentNode)))
            if (pos == idx)
              ops += NoOp()
            else
              ops += MoveOp(idx)
          }

        case _ =>
          // if current node has a key that is still upcoming, do not replace but insert
          val currentNode = newNodes(pos)
          if (currentNode.blueprint.key.exists(nextKeys.contains)) {
            val newNode = updateBranch(None, bp, Some(currentNode))
            newNodes.insert(pos, newNode)
            ops += InsertOp(newNode.getId)
          } else {
            val newNode = updateBranch(Some(currentNode), bp, Some(currentNode))
            if (newNode eq currentNode) {
              ops += NoOp()
            } else {
              newNodes(pos).destroy()
              newNodes(pos) = newNode
              ops += ReplaceOp(newNode.getId)
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
      ops += RemoveOp(newNodes.size - pos)
      newNodes.drop(pos).foreach(_.destroy())
      newNodes.remove(pos, newNodes.size - pos)
    }
    // compress NoOps
    val finalOps: Seq[ChildOp] = if (ops.size > 1) {
      val compressed = ops.tail.foldLeft(mutable.ListBuffer[ChildOp](ops.head)) { (res, op) =>
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

object UIManagerProxy {

  protected[suzaku] var internalUiChannel = null: MessageChannel[UIProtocol.type]

  protected var lastComponentId = 0

  sealed abstract class ShadowNode(val parent: Option[ShadowNode]) {
    type BP <: Blueprint
    var blueprint: BP

    def getWidget: ShadowWidget

    def getId: Int = getWidget.widgetId

    def key: Iterable[Any] = blueprint.key

    def destroy(): Unit
  }

  private[suzaku] final class EmptyNode(parent: Option[ShadowNode]) extends ShadowNode(parent) {
    type BP = EmptyBlueprint.type

    override var blueprint               = EmptyBlueprint
    override def getWidget: ShadowWidget = null
    override def getId: Int              = -1
    override def destroy(): Unit         = {}
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

    val proxy    = blueprint.createProxy(widgetId, uiChannel).asInstanceOf[WidgetProxy[WidgetProtocol, WidgetBlueprint]]
    var children = Seq.empty[ShadowNode]

    override def getWidget: ShadowWidget = this

    override def destroy(): Unit = {
      children.foreach(_.destroy())
      proxy.destroyWidget()
    }

    def withChildren(c: Seq[ShadowNode]): ShadowWidget = {
      children = c
      this
    }

    override def toString: String = s"ShadowWidget($blueprint, $widgetId)"
  }

  private[suzaku] final class ShadowComponent(var blueprint: ComponentBlueprint,
                                              innerBuilder: (Blueprint, Option[ShadowNode]) => ShadowNode,
                                              parent: Option[ShadowNode],
                                              enqueueStateMod: (ShadowComponent, () => Unit) => Unit)
      extends ShadowNode(parent)
      with StateProxy {
    type BP = ComponentBlueprint

    val component = blueprint.create.asInstanceOf[Component[ComponentBlueprint, Any]]
    component.setProxy(this)
    val componentId: List[Int] = {
      lastComponentId += 1
      lastComponentId :: findParentComponentId(parent)
    }
    var state       = component.initialState
    var rendered    = component.render(state)
    var inner       = innerBuilder(rendered, Some(this))
    var isDirty     = false
    var nextState   = state
    var isDestroyed = false
    component.didMount()

    @tailrec
    private def findParentComponentId(parent: Option[ShadowNode]): List[Int] = {
      parent match {
        case None                      => Nil
        case Some(pc: ShadowComponent) => pc.componentId
        case Some(other)               => findParentComponentId(other.parent)
      }
    }

    override def getWidget: ShadowWidget = inner.getWidget

    override def modState[S](f: (S) => S): Unit = {
      enqueueStateMod(this, () => nextState = f(nextState.asInstanceOf[S]))
    }

    override def destroy(): Unit = {
      inner.destroy()
      component.didUnmount()
      isDestroyed = true
    }

    override def toString: String = s"ShadowComponent($blueprint)"
  }

  private var widgetClasses = Map.empty[String, Int]
  private var widgetClassId = 0

  def getWidgetClass(widgetProtocol: WidgetProtocol): Int = {
    this.synchronized {
      val name = widgetProtocol.widgetName
      widgetClasses.get(name) match {
        case Some(id) => id
        case None     =>
          // create integer mapping and register the class
          val id = widgetClassId
          widgetClassId += 1
          widgetClasses += name -> id
          internalUiChannel.send(RegisterWidgetClass(name, id))
          id
      }
    }
  }
}
