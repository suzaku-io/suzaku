package suzaku.ui.style

object StyleRegistry {
  type StyleRegistration = (Int, List[StyleProperty])

  var styleClassId         = 1
  var pendingRegistrations = List.empty[StyleRegistration]

  def register(style: List[StyleProperty]): Int = {
    this.synchronized {
      val id = styleClassId
      styleClassId += 1
      pendingRegistrations ::= id -> style
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
