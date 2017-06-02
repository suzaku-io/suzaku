package suzaku.ui

import arteria.core._
import suzaku.platform.Logger
import suzaku.ui.UIProtocol._
import suzaku.ui.style.StyleRegistry

import scala.collection.mutable
import scala.collection.immutable

class UIManager(logger: Logger, channelEstablished: UIChannel => Unit, flushMessages: () => Unit)
    extends MessageChannelHandler[UIProtocol.type] {
  import UIManager._

  private var lastFrame      = 0L
  protected var uiChannel    = null: MessageChannel[ChannelProtocol]
  private var currentRoot    = Option.empty[ShadowNode]
  private[suzaku] var viewId = 1
  private var dirtyRoots     = List.empty[ShadowComponent]
  private var frameRequested = false

  private def allocateId: Int = {
    val id = viewId
    viewId += 1
    id
  }

  protected def send[A <: Message](message: A)(implicit ev: MessageWitness[A, ChannelProtocol]) = {
    uiChannel.send(message)
  }

  override def process = {
    case NextFrame(time) =>
      lastFrame = time
      frameRequested = false
      // check if styles have updated
      if (StyleRegistry.hasRegistrations) {
        val styles = StyleRegistry.dequeueRegistrations
        logger.debug(s"Adding ${styles.size} styles")
        send(AddStyles(styles))
      }
      // update dirty component trees
      if (dirtyRoots.nonEmpty)
        logger.debug(s"Updating ${dirtyRoots.size} dirty components")

      dirtyRoots.foreach { shadowComponent =>
        updateBranch(Some(shadowComponent), shadowComponent.blueprint, shadowComponent.parent)
      }
      dirtyRoots = Nil
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

  def render(root: Blueprint): Unit = {
    val newRoot = updateBranch(currentRoot, root, None)
    currentRoot match {
      case Some(view) if view.getId == newRoot.getId =>
      // no-op
      case Some(view) =>
        logger.debug(s"Replacing root [${view.getId}] with [${newRoot.getId}]")
        view.destroy()
        send(MountRoot(newRoot.getId))
      case None =>
        logger.debug(s"Mounting [${newRoot.getId}] as root")
        send(MountRoot(newRoot.getId))
    }
    currentRoot = Some(newRoot)
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

  private def updateBranch(current: Option[ShadowNode], blueprint: Blueprint, parent: Option[ShadowNode]): ShadowNode = {
    current match {
      case None =>
        // nothing to compare to, just keep rendering
        blueprint match {
          case EmptyBlueprint =>
            EmptyNode

          case viewBP: WidgetBlueprint =>
            val viewId     = allocateId
            val shadowView = new ShadowWidget(viewBP, viewId, parent, uiChannel)
            if (viewBP.children.nonEmpty) {
              val children = viewBP.children.map(c => updateBranch(None, c, Some(shadowView)))
              send(SetChildren(viewId, children.flatMap(expand).map(_.getId)))
              shadowView.withChildren(children)
            } else {
              shadowView
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
        // there's an existing view node here that should be updated/replaced
        blueprint match {
          case EmptyBlueprint =>
            EmptyNode

          case viewBP: WidgetBlueprint if viewBP.getClass eq node.blueprint.getClass =>
            val (newChildren, ops) = updateChildren(node, node.children.flatMap(expand), viewBP.children.flatMap(expandBP))
            if (ops.nonEmpty)
              send(UpdateChildren(node.widgetId, ops))
            node.children = newChildren
            if (viewBP sameAs node.blueprint.asInstanceOf[viewBP.This]) {
              // no change, return current node
              node
            } else {
              // update node
              node.proxy.update(viewBP)
              node.blueprint = viewBP
              node
            }

          case _ =>
            // always replace when different view or component
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
            // always replace when different view or component
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

    def getView: ShadowWidget

    def getId: Int = getView.widgetId

    def key: Iterable[Any] = blueprint.key

    def destroy(): Unit
  }

  private[suzaku] final object EmptyNode extends ShadowNode(None) {
    type BP = EmptyBlueprint.type

    override var blueprint             = EmptyBlueprint
    override def getView: ShadowWidget = null
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

    override def getView: ShadowWidget = throw new NotImplementedError("ShadowNodeSeq does not have a view")

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

    override def getView: ShadowWidget = this

    override def destroy(): Unit = {
      children.foreach(_.destroy())
      proxy.destroyView()
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

    override def getView: ShadowWidget = inner.getView

    override def modState[S](f: (S) => S): Unit = {
      nextState = f(nextState.asInstanceOf[S])
      if (!isDirty) {
        addDirtyRoot(this)
      }
      isDirty = true
    }

    override def destroy(): Unit = {
      inner.destroy()
      component.didUnmount()
    }

    override def toString: String = s"ShadowComponent($blueprint)"
  }

}
