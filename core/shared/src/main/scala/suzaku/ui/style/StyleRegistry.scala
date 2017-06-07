package suzaku.ui.style

object StyleRegistry {
  type StyleRegistration = (Int, List[StyleProperty])

  var styleClassId         = 1
  var pendingRegistrations = List.empty[StyleRegistration]

  def register(style: List[StyleDef]): Int = {
    val styles = style.flatMap {
      case StyleSeq(seq) =>
        seq
      case s: StyleProperty =>
        s :: Nil
    }
    this.synchronized {
      val id = styleClassId
      styleClassId += 1
      pendingRegistrations ::= id -> styles
      id
    }
  }

  def hasRegistrations = pendingRegistrations.nonEmpty

  def dequeueRegistrations: List[StyleRegistration] = {
    this.synchronized {
      val regs = pendingRegistrations
      pendingRegistrations = Nil
      regs
    }
  }
}
